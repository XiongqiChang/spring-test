package com.thoughtworks.rslist.domain;

import com.thoughtworks.rslist.dto.RsEventDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Trade {

    @NotNull
    @Min(value = 0)
    private Integer amount;

    @NotNull
    @Min(value = 1)
    private Integer rank;


}
