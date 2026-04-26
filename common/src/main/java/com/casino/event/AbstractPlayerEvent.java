package com.casino.event;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbstractPlayerEvent {

    @NotNull(message = "playerProfileId should be specified")
    public Integer playerProfileId;

}
