package ong.aurora.model.v_0_0_1.command.init;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InitCommandData(@JsonProperty("person_name") String personName,
                              @JsonProperty("person_lastname") String personLastname,
                              @JsonProperty("person_mail") String personMail) {
}
