package com.matchi

import com.fasterxml.jackson.annotation.JsonIgnore

abstract class ErrorsMixIn {
    @JsonIgnore abstract getErrors()
}