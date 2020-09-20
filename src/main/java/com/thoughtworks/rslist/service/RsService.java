package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RsService {
  final RsEventRepository rsEventRepository;
  final UserRepository userRepository;
  final VoteRepository voteRepository;
  final TradeRepository tradeRepository;

  public RsService(RsEventRepository rsEventRepository, UserRepository userRepository, VoteRepository voteRepository, TradeRepository tradeRepository) {
    this.rsEventRepository = rsEventRepository;
    this.userRepository = userRepository;
    this.voteRepository = voteRepository;
    this.tradeRepository = tradeRepository;
  }

  public void vote(Vote vote, int rsEventId) {
    Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
    Optional<UserDto> userDto = userRepository.findById(vote.getUserId());
    if (!rsEventDto.isPresent()
            || !userDto.isPresent()
            || vote.getVoteNum() > userDto.get().getVoteNum()) {
      throw new RuntimeException();
    }
    VoteDto voteDto =
            VoteDto.builder()
                    .localDateTime(vote.getTime())
                    .num(vote.getVoteNum())
                    .rsEvent(rsEventDto.get())
                    .user(userDto.get())
                    .build();
    voteRepository.save(voteDto);
    UserDto user = userDto.get();
    user.setVoteNum(user.getVoteNum() - vote.getVoteNum());
    userRepository.save(user);
    RsEventDto rsEvent = rsEventDto.get();
    rsEvent.setVoteNum(rsEvent.getVoteNum() + vote.getVoteNum());
    rsEventRepository.save(rsEvent);
  }

  public boolean buy(Trade trade, int id) {
    Optional<RsEventDto> rsEventDto = rsEventRepository.findById(id);
    if (rsEventDto.isPresent()) {
      RsEventDto rsEventDto1 = rsEventDto.get();
      TradeDto byRank = tradeRepository.findByRank(trade.getRank());
      if (byRank == null) {
        List<TradeDto> byRsEventId = tradeRepository.findByRsEventId(id);
        if (byRsEventId.size() != 0){
          Integer id1 = byRsEventId.get(0).getId();
          tradeRepository.deleteById(id1);
        }
        TradeDto tradeDto = TradeDto.builder().amount(trade.getAmount()).rank(trade.getRank())
                .rsEvent(rsEventDto1).build();
        rsEventDto1.setRankNum(trade.getRank());
        tradeRepository.save(tradeDto);
        rsEventRepository.save(rsEventDto1);
        updateRsEventRankNum();
        return true;
      } else {
        if (byRank.getAmount() >= trade.getAmount()) {
          return false;
        } else {
          TradeDto tradeDto = TradeDto.builder().amount(trade.getAmount()).rank(trade.getRank())
                  .rsEvent(rsEventDto1).build();
          tradeRepository.save(tradeDto);
          rsEventDto1.setRankNum(trade.getRank());
          tradeRepository.save(tradeDto);
          rsEventRepository.deleteById(byRank.getRsEvent().getId());
          updateRsEventRankNum();
          return true;
        }
      }
    } else {
      return false;
    }
  }

  public void updateRsEventRankNum() {
    List<RsEventDto> all = rsEventRepository.findAll();
    List<Integer> rankList = new ArrayList<>();
    List<TradeDto> tradeDtoList = tradeRepository.findAll();
    for (TradeDto tradeDto : tradeDtoList) {
      int id = tradeDto.getRsEvent().getId();
      RsEventDto rsEventDto = rsEventRepository.findById(id).get();
      rsEventDto.setRankNum((tradeDto.getRank()));
      rsEventRepository.save(rsEventDto);
      rankList.add(tradeDto.getRank());
    }
    List<RsEventDto> collect = all.stream()
            .sorted(Comparator.comparingInt(RsEventDto::getVoteNum).reversed())
            .collect(Collectors.toList());
    for (int i = 0; i < collect.size(); i++) {
      RsEventDto rsEventDto = collect.get(i);
      int rsEventDtoId = rsEventDto.getId();
      if (tradeRepository.findByRsEventId(rsEventDtoId).size() == 0) {
        int indexOf = collect.indexOf(rsEventDto);
        while (rankList.contains(indexOf + 1)) {
          indexOf = indexOf + 1;
        }
        rsEventDto.setRankNum(indexOf + 1);
        rankList.add(indexOf+ 1);
        rsEventRepository.save(rsEventDto);
      }
    }
  }
}
