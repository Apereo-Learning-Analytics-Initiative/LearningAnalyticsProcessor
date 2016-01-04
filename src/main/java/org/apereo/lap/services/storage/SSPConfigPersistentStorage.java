/**
 * 
 */
package org.apereo.lap.services.storage;

import org.apereo.lap.model.SSPConfig;

/**
 * @author ggilbert
 *
 */
public interface SSPConfigPersistentStorage {
  SSPConfig save(SSPConfig sspConfig);
  SSPConfig get();
}
