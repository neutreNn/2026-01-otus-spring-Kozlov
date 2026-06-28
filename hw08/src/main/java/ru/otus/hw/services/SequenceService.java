package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import ru.otus.hw.models.DatabaseSequence;

@RequiredArgsConstructor
@Service
public class SequenceService {
    private final MongoOperations mongoOperations;

    public long getNextSequence(String sequenceName) {
        var query = Query.query(Criteria.where("_id").is(sequenceName));
        var update = new Update().inc("sequence", 1);
        var options = FindAndModifyOptions.options().returnNew(true).upsert(true);
        var sequence = mongoOperations.findAndModify(query, update, options, DatabaseSequence.class);
        return sequence == null ? 1 : sequence.getSequence();
    }
}
