package com.thoughtworks.rslist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @Author: xqc
 * @Date: 2020/9/19 - 09 - 19 - 22:16
 * @Description: com.thoughtworks.rslist.dto
 * @version: 1.0
 */
@Entity
@Data
@Table(name = "trade")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeDto {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "rank")
    private Integer rank;

    @ManyToOne
    private RsEventDto rsEvent;


}
