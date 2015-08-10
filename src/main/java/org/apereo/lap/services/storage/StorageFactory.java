/**
 * 
 */
package org.apereo.lap.services.storage;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author ggilbert
 *
 */
@Component
public class StorageFactory {
  @Value("${lap.persistentStorage:H2}")
  private String persistentStorage;
  
  @Autowired private Map<String, PersistentStorage<ModelOutput>> persistentStorageOptions;
  @Autowired private Map<String, ModelRunPersistentStorage> modelRunPersistentStorageOptions;
  
  public PersistentStorage<ModelOutput> getPersistentStorage() {
    return persistentStorageOptions.get(persistentStorage);
  }
  
  public ModelRunPersistentStorage getModelRunPersistentStorage() {
    
    String key = persistentStorage + "-ModelRunPersistentStorage";
    
    return modelRunPersistentStorageOptions.get(key);
  }
}
