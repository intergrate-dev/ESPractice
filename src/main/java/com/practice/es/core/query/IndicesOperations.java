package com.practice.es.core.query;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;

public interface IndicesOperations {

    CreateIndexResponse create(CreateIndexRequest request);

    void create(CreateIndexRequest request, ActionListener<CreateIndexResponse> listener);

    AcknowledgedResponse delete(DeleteIndexRequest request);

    void delete(DeleteIndexRequest request, ActionListener<AcknowledgedResponse> listener);
}
