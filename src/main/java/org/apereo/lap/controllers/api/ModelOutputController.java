/**
 * 
 */
package org.apereo.lap.controllers.api;

import java.util.ArrayList;

import org.apereo.lap.model.api.ModelOutputRecord;
import org.apereo.lap.model.api.ModelOutputResourceAssembler;
import org.apereo.lap.services.storage.ModelOutput;
import org.apereo.lap.services.storage.PersistentStorage;
import org.apereo.lap.services.storage.StorageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ggilbert
 *
 */
@RestController
public class ModelOutputController {
  
  @Autowired
  private StorageFactory storageFactory;
  
  @Autowired
  private ModelOutputResourceAssembler modelOutputResourceAssembler;
  
  @RequestMapping(method = RequestMethod.GET, produces = {"application/json"}, value="/api/output/{tenant}")
  public PagedResources<ModelOutputRecord> output(@PageableDefault(size = 100, page = 0) Pageable pageable, 
      PagedResourcesAssembler<ModelOutput> assembler) {
    
    PersistentStorage<ModelOutput> persistentStorage = storageFactory.getPersistentStorage();
    Page<ModelOutput> output = persistentStorage.findAll(pageable);
    if (output == null) {
      output = new PageImpl<ModelOutput>(new ArrayList<ModelOutput>(), pageable, 0);
    }
    return assembler.toResource(output, modelOutputResourceAssembler);
  }
  
  @RequestMapping(method = RequestMethod.GET, produces = {"application/json"}, value="/api/output/{tenant}/student/{id}")
  public PagedResources<ModelOutputRecord> outputByStudent(@PageableDefault(size = 100, page = 0) Pageable pageable, 
      PagedResourcesAssembler<ModelOutput> assembler, @PathVariable("id") final String id) {
    
    PersistentStorage<ModelOutput> persistentStorage = storageFactory.getPersistentStorage();
    Page<ModelOutput> output = persistentStorage.findByStudentId(id, pageable);
    if (output == null) {
      output = new PageImpl<ModelOutput>(new ArrayList<ModelOutput>(), pageable, 0);
    }
    return assembler.toResource(output, modelOutputResourceAssembler);
  }
  
  @RequestMapping(method = RequestMethod.GET, produces = {"application/json"}, value="/api/output/{tenant}/course/{id}")
  public PagedResources<ModelOutputRecord> outputByCourse(@PageableDefault(size = 100, page = 0) Pageable pageable, 
      PagedResourcesAssembler<ModelOutput> assembler, @PathVariable("id") final String id, @RequestParam(required=false,value="lastRunOnly") boolean lastRunOnly) {
    
    PersistentStorage<ModelOutput> persistentStorage = storageFactory.getPersistentStorage();
    Page<ModelOutput> output = persistentStorage.findByCourseId(id, lastRunOnly, pageable);
    if (output == null) {
      output = new PageImpl<ModelOutput>(new ArrayList<ModelOutput>(), pageable, 0);
    }
    return assembler.toResource(output, modelOutputResourceAssembler);
  }
  
  @RequestMapping(method = RequestMethod.GET, produces = {"application/json"}, value="/api/output/{tenant}/course/{courseId}/student/{studentId}")
  public PagedResources<ModelOutputRecord> outputByStudentAndCourse(@PageableDefault(size = 100, page = 0) Pageable pageable, 
      PagedResourcesAssembler<ModelOutput> assembler, @PathVariable("courseId") final String courseId, @PathVariable("studentId") final String studentId, @RequestParam(required=false,value="lastRunOnly") boolean lastRunOnly) {
    
    PersistentStorage<ModelOutput> persistentStorage = storageFactory.getPersistentStorage();
    Page<ModelOutput> output = persistentStorage.findByStudentIdAndCourseId(studentId, courseId, lastRunOnly, pageable);
    if (output == null) {
      output = new PageImpl<ModelOutput>(new ArrayList<ModelOutput>(), pageable, 0);
    }
    return assembler.toResource(output, modelOutputResourceAssembler);
  }
  
}
