package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.VoteDto;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @Author: xqc
 * @Date: 2020/9/20 - 09 - 20 - 10:15
 * @Description: com.thoughtworks.rslist.repository
 * @version: 1.0
 */
public interface TradeRepository extends CrudRepository<TradeDto,Integer> {


    List<TradeDto> findByRsEventId(Integer rsEventId);

    TradeDto  findByRank(int rank);

   @Override
   List<TradeDto> findAll();
}
