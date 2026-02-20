package com.vantage.api.dto;

import java.io.Serializable;

/**
 *  Represents the message payload sent to Redis for the worker to process.
 *  Using a Record automatically provides a constructor, getters, equals,
 *  and hashCode without boilerplate.
 *  @param id
 *  @param url
 */

public record LinkValidationTask(
        Long id,
        String url
) implements Serializable  {}
