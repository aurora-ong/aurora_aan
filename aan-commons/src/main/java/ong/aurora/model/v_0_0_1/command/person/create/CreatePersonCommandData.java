package ong.aurora.model.v_0_0_1.command.person.create;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreatePersonCommandData(@JsonProperty("person_id") String personId,
                                      @JsonProperty("person_name") String personName,
                                      @JsonProperty("person_lastname") String personLastname,
                                      @JsonProperty("person_mail") String personMail) {
}
