package ee.fujitsu.movieapi.rest.api.response;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import javax.validation.constraints.NotNull;

public abstract class AbstractResponse {
    @JsonProperty
    @NotNull
    private	ResponseCode responseCode = ResponseCode.OK;
    @JsonProperty
    private	String message;

    public ResponseCode getResponseCode() {
        return responseCode;
    }

    public void setResponseCode( ResponseCode responseCode ) {
        this.responseCode = responseCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage( String message ) {
        this.message = message;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper( this ).omitNullValues()
                .add( "responseCode", responseCode )
                .add( "message", message )
                .toString();
    }
}
