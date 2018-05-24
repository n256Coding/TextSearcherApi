package com.n256coding.MongoRepositories;

import com.n256coding.DatabaseModels.Resource;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResourceRepository extends MongoRepository<Resource, String> {

}
