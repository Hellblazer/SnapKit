package snap.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import snap.util.CSVReader;
import snap.util.ListUtils;
import snap.util.SnapUtils;
import snap.util.StringUtils;
import snap.web.WebFile;

/**
 * A DataSite for flat files databases.
 */
public class FileDataSite extends DataSite {

    // The map of row-lists generated from import
    Map <Entity, List <Row>>      _entityRows = new HashMap();
    
    // Dirty entity set
    Set <Entity>                  _dirtyEntities = new HashSet();
    
    // Table data file extension
    static final String           TableEntityFileExt = ".entity"; 
    static final String           TableDataFileExt = ".csv"; 

/**
 * Get entity by loading from entity file.
 */
protected Entity getEntityImpl(String aName) throws Exception
{
    Entity entity = super.getEntityImpl(aName); if(entity!=null) return entity;
    WebFile entityFile = getTableEntityFile(aName, false); if(entityFile==null) return null;
    entity = createEntity(aName);
    entity.fromBytes(entityFile.getBytes());
    return entity;
}

/**
 * Save entity by saving entity bytes to entity file.
 */
protected void saveEntityImpl(Entity anEntity) throws Exception
{
    super.saveEntityImpl(anEntity);
    WebFile entityFile = getTableEntityFile(anEntity.getName(), true);
    entityFile.setBytes(anEntity.toBytes());
    entityFile.save();
}

/**
 * Delete entity file and entity table data file.
 */
protected void deleteEntityImpl(Entity anEntity) throws Exception
{
    super.deleteEntityImpl(anEntity);
    WebFile efile = getTableEntityFile(anEntity.getName(), false);
    if(efile!=null)
        efile.delete();
    WebFile tfile = getTableDataFile(anEntity.getName(), false);
    if(tfile!=null)
        tfile.delete();
}

/**
 * Returns a set of rows for the given properties and condition.
 */
protected List <Row> getRowsImpl(Entity anEntity, Query aQuery)
{
    // Get entity rows
    Condition condition = aQuery.getCondition();
    Row entityRows[] = getEntityRows(anEntity).toArray(new Row[0]);
    
    // Create fetch list and add rows that satisfy condition
    List <Row> rows = new ArrayList();
    for(Row row : entityRows)
        if(condition==null || condition.getValue(anEntity, row))
            rows.add(row);
    
    // Return rows
    return rows;
}

/**
 * Inserts or updates a given row.
 */
protected void saveRowImpl(Row aRow)
{
    // Get row entity
    Entity entity = aRow.getEntity();
    
    // If row hasn't been saved yet, insert into entity rows and update any auto generated properties
    if(!aRow.getExists()) {
        
        // Add to entity rows
        List <Row> entityRows = getEntityRows(entity);
        entityRows.add(aRow);
    
        // Set auto-generated properties
        for(Property property : entity.getProperties())
            if(property.isAutoGenerated()) {
                int maxID = 0; for(Row row : entityRows) maxID = Math.max(maxID,SnapUtils.intValue(row.get(property)));
                aRow.put(property, maxID + 1);
            }
    }
    
    // Add dirty entity
    synchronized (this) { _dirtyEntities.add(entity); }
}

/**
 * Deletes a given row.
 */
protected void deleteRowImpl(Row aRow)
{
    // Get EntityRows list for Entity and row for PrimaryValue (just return if no row for PrimaryValue)
    Entity entity = aRow.getEntity();
    List <Row> entityRows = getEntityRows(entity);
    
    // Remove row and add row entity to DirtyEntities set
    ListUtils.removeId(entityRows, aRow);
    synchronized (this) { _dirtyEntities.add(entity); }
}

/**
 * Save entity files for changed entities.
 */
protected void saveEntityFiles() throws Exception
{
    // Copy and clear DirtyEntities
    Entity entities[];
    synchronized (this) {
        entities = _dirtyEntities.toArray(new Entity[_dirtyEntities.size()]);
        _dirtyEntities.clear();        
    }

    // Save files
    for(Entity entity : entities) saveEntityFile(entity);
}

/**
 * Save entity files for changed entities.
 */
protected void saveEntityFile(Entity anEntity) throws Exception
{
    // Get entity rows and StringBuffer
    Row entityRows[] = getEntityRows(anEntity).toArray(new Row[0]);
    StringBuffer sbuffer = new StringBuffer();
    
    // Iterate over properties and add header row
    for(Property property : anEntity.getProperties())
        if(!property.isDerived())
            sbuffer.append(StringUtils.getStringQuoted(property.getName())).append(", ");
    
    // Replace trailing field delimiter with record delimiter
    sbuffer.delete(sbuffer.length()-2, sbuffer.length()).append("\n");

    // Iterate over rows
    for(Row row : entityRows) {
    
        // Iterate over properties
        for(Property property : anEntity.getProperties()) {
            
            // Skip derived properties
            if(property.isDerived()) continue;
            
            // Get value and string
            Object value = row.getValue(property);
            String string = (String)DataUtils.convertValue(value, Property.Type.String);
            if(string==null)
                string = "";
            
            // Write string
            sbuffer.append(StringUtils.getStringQuoted(string)).append(", ");
        }
        
        // Replace trailing field delimiter with record delimiter
        sbuffer.delete(sbuffer.length()-2, sbuffer.length()).append("\n");
    }
    
    // Get entity file, set bytes and save
    WebFile entityFile = getTableDataFile(anEntity.getName(), true);
    byte bytes[] = StringUtils.getBytes(sbuffer.toString()); entityFile.setBytes(bytes);
    entityFile.save();
}

/**
 * Override to Saves changes if any made.
 */
public void flush() throws Exception
{
    super.flush();
    if(_dirtyEntities.size()>0)
        saveEntityFiles();
}

/**
 * Returns the file for the given entity.
 */
protected WebFile getTableEntityFile(String aName, boolean doCreate)
{
    String path = "/FileDB/" + aName + TableEntityFileExt;
    WebFile tfile = _wsite.getSandbox().getFile(path);
    if(tfile==null && doCreate) tfile = _wsite.getSandbox().createFile(path, false);
    return tfile;
}

/**
 * Returns the file for the given entity.
 */
protected WebFile getTableDataFile(String aName, boolean doCreate)
{
    String path = "/FileDB/" + aName + TableDataFileExt;
    WebFile tfile = _wsite.getSandbox().getFile(path);
    if(tfile==null && doCreate) tfile = _wsite.getSandbox().createFile(path, false);
    return tfile;
}

/**
 * Returns the list of rows for a given entity.
 */
protected synchronized List <Row> getEntityRows(Entity anEntity)
{
    // Get entity rows
    List <Row> entityRows = _entityRows.get(anEntity);
    if(entityRows!=null)
        return entityRows;
    
    // Create and set entity rows list
    _entityRows.put(anEntity, entityRows = Collections.synchronizedList(new ArrayList()));
    
    // Get entity file
    WebFile entityFile = getTableDataFile(anEntity.getName(), false);
    
    // If file exists, read rows
    if(entityFile!=null) {
        
        // Create CSVReader
        CSVReader csvReader = new CSVReader();
        csvReader.setFieldDelimiter(",");
        csvReader.setHasHeaderRow(true);
        csvReader.setHasQuotedFields(true);
        
        // Read maps
        List <Map> maps = csvReader.readObject(entityFile.getBytes(), anEntity.getName(), false);
        
        // Create rows for maps and add to entityRows list
        Property primaryProperty = anEntity.getPrimary();
        for(Map map : maps) {
            Object pvalue = primaryProperty.convertValue(map.get(primaryProperty.getName()));
            Row row = createRow(anEntity, pvalue, map);
            entityRows.add(row);
        }
    }
    
    // Return entity rows
    return entityRows;
}

}