package com.thoughtworks.rslist.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RsControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired UserRepository userRepository;
  @Autowired RsEventRepository rsEventRepository;
  @Autowired VoteRepository voteRepository;
  @Autowired
  TradeRepository tradeRepository;
  private UserDto userDto;


  @BeforeEach
  void setUp() {
    voteRepository.deleteAll();
    rsEventRepository.deleteAll();
    userRepository.deleteAll();
    userDto =
        UserDto.builder()
            .voteNum(10)
            .phone("188888888888")
            .gender("female")
            .email("a@b.com")
            .age(19)
            .userName("idolice")
            .build();
  }

  @Test
  public void shouldGetRsEventList() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).voteNum(15).build();

    rsEventRepository.save(rsEventDto);
    RsEventDto rsEventDto1 =
        RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).voteNum(10).build();

    rsEventRepository.save(rsEventDto1);
    RsEventDto rsEventDto2 =
        RsEventDto.builder().keyword("无分类").eventName("第三条事件").user(save).voteNum(30).build();

    rsEventRepository.save(rsEventDto2);

    mockMvc
        .perform(get("/rs/list"))
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].eventName", is("第三条事件")))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[0]", not(hasKey("user"))))
        .andExpect(status().isOk());
  }

  @Test
  public void shouldGetOneEvent() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.eventName", is("第一条事件")));
    mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.keyword", is("无分类")));
    mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.eventName", is("第二条事件")));
    mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.keyword", is("无分类")));
  }

  @Test
  public void shouldGetErrorWhenIndexInvalid() throws Exception {
    mockMvc
        .perform(get("/rs/4"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("invalid index")));
  }

  @Test
  public void shouldGetRsListBetween() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第三条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    mockMvc
        .perform(get("/rs/list?start=1&end=2"))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")));
    mockMvc
        .perform(get("/rs/list?start=2&end=3"))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第三条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")));
    mockMvc
        .perform(get("/rs/list?start=1&end=3"))
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")))
        .andExpect(jsonPath("$[2].eventName", is("第三条事件")))
        .andExpect(jsonPath("$[2].keyword", is("无分类")));
  }

  @Test
  public void shouldAddRsEventWhenUserExist() throws Exception {

    UserDto save = userRepository.save(userDto);

    String jsonValue =
        "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": " + save.getId() + "}";

    mockMvc
        .perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
    List<RsEventDto> all = rsEventRepository.findAll();
    assertNotNull(all);
    assertEquals(all.size(), 1);
    assertEquals(all.get(0).getEventName(), "猪肉涨价了");
    assertEquals(all.get(0).getKeyword(), "经济");
    assertEquals(all.get(0).getUser().getUserName(), save.getUserName());
    assertEquals(all.get(0).getUser().getAge(), save.getAge());
  }

  @Test
  public void shouldAddRsEventWhenUserNotExist() throws Exception {
    String jsonValue = "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": 100}";
    mockMvc
        .perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldVoteSuccess() throws Exception {
    UserDto save = userRepository.save(userDto);
    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();
    rsEventDto = rsEventRepository.save(rsEventDto);

    String jsonValue =
        String.format(
            "{\"userId\":%d,\"time\":\"%s\",\"voteNum\":1}",
            save.getId(), LocalDateTime.now().toString());
    mockMvc
        .perform(
            post("/rs/vote/{id}", rsEventDto.getId())
                .content(jsonValue)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    UserDto userDto = userRepository.findById(save.getId()).get();
    RsEventDto newRsEvent = rsEventRepository.findById(rsEventDto.getId()).get();
    assertEquals(userDto.getVoteNum(), 9);
    assertEquals(newRsEvent.getVoteNum(), 1);
    List<VoteDto> voteDtos =  voteRepository.findAll();
    assertEquals(voteDtos.size(), 1);
    assertEquals(voteDtos.get(0).getNum(), 1);
  }

  @Test
  void shouldBuyRsEventButNotHasAnyBought() throws  Exception {
    UserDto save = userRepository.save(userDto);
    RsEventDto rsEventDto =
            RsEventDto.builder().keyword("健康").eventName("第一条事件").user(save).voteNum(12).rankNum(4).build();
    rsEventRepository.save(rsEventDto);
    RsEventDto rsEventDto1 =
            RsEventDto.builder().keyword("经济").eventName("猪肉涨价").user(save).voteNum(12).rankNum(2).build();

    rsEventRepository.save(rsEventDto1);
    ObjectMapper objectMapper = new ObjectMapper();
    Trade build = Trade.builder().amount(100).rank(1).build();
    String string = objectMapper.writeValueAsString(build);
    mockMvc.perform(post("/rs/buy/"+rsEventDto.getId()).content(string).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
           // .andExpect(jsonPath("$.rankNum",is(1)));
  }
  @Test
  void shouldBuyRsEventSuccessSuperMoney() throws Exception {
    UserDto save = userRepository.save(userDto);
    RsEventDto rsEventDto =
            RsEventDto.builder().keyword("健康").eventName("第一条事件").user(save).voteNum(12).rankNum(4).build();
    rsEventRepository.save(rsEventDto);
    RsEventDto rsEventDto1 =
            RsEventDto.builder().keyword("经济").eventName("猪肉涨价").user(save).voteNum(12).rankNum(2).build();

    rsEventRepository.save(rsEventDto1);
    ObjectMapper objectMapper = new ObjectMapper();

    Trade build = Trade.builder().amount(100).rank(1).build();
    String string = objectMapper.writeValueAsString(build);
    mockMvc.perform(post("/rs/buy/"+rsEventDto.getId()).content(string).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    Trade build1 = Trade.builder().amount(200).rank(1).build();
    String string1 = objectMapper.writeValueAsString(build1);
    mockMvc.perform(post("/rs/buy/"+rsEventDto1.getId()).content(string1).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    assertEquals(Optional.empty(),rsEventRepository.findById(rsEventDto.getId()));


  }
  @Test
  void shouldThrowException() throws Exception {
    UserDto save = userRepository.save(userDto);
    RsEventDto rsEventDto =
            RsEventDto.builder().keyword("健康").eventName("第一条事件").user(save).voteNum(12).build();
    rsEventRepository.save(rsEventDto);
    RsEventDto rsEventDto1 =
            RsEventDto.builder().keyword("经济").eventName("猪肉涨价").user(save).voteNum(12).build();
    rsEventRepository.save(rsEventDto1);
    ObjectMapper objectMapper = new ObjectMapper();
    Trade build = Trade.builder().amount(100).rank(1).build();
    String string = objectMapper.writeValueAsString(build);
    mockMvc.perform(post("/rs/buy/-1").content(string).contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error",is("invalid param")));
  }

  @Test
  void shouldBuyRsEventSuccessHasBought() throws Exception {
    UserDto save = userRepository.save(userDto);
    RsEventDto rsEventDto =
            RsEventDto.builder().keyword("健康").eventName("第一条事件").user(save).voteNum(12).rankNum(4).build();
    rsEventRepository.save(rsEventDto);
    RsEventDto rsEventDto1 =
            RsEventDto.builder().keyword("经济").eventName("猪肉涨价").user(save).voteNum(12).rankNum(2).build();

    rsEventRepository.save(rsEventDto1);
    ObjectMapper objectMapper = new ObjectMapper();

    Trade build = Trade.builder().amount(100).rank(1).build();
    String string = objectMapper.writeValueAsString(build);
    mockMvc.perform(post("/rs/buy/"+rsEventDto.getId()).content(string).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    Trade build1 = Trade.builder().amount(20).rank(1).build();
    String string1 = objectMapper.writeValueAsString(build1);
    mockMvc.perform(post("/rs/buy/"+rsEventDto1.getId()).content(string1).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    int rankNum = rsEventRepository.findById(rsEventDto1.getId()).get().getRankNum();
    assertEquals(2,rankNum);
  }

  @Test
  void shouldGetRsEventByRankNumAfterTrade() throws Exception {
    UserDto save = userRepository.save(userDto);
    RsEventDto rsEventDto =
            RsEventDto.builder().keyword("健康").eventName("第一条事件").user(save).voteNum(12).rankNum(4).build();
    rsEventRepository.save(rsEventDto);
    RsEventDto rsEventDto1 =
            RsEventDto.builder().keyword("经济").eventName("猪肉涨价").user(save).voteNum(22).rankNum(2).build();

    rsEventRepository.save(rsEventDto1);

    RsEventDto rsEventDto2 =
            RsEventDto.builder().keyword("健康").eventName("锻炼身体").user(save).voteNum(30).rankNum(4).build();
    rsEventRepository.save(rsEventDto2);
    ObjectMapper objectMapper = new ObjectMapper();

    Trade build = Trade.builder().amount(100).rank(1).build();
    String string = objectMapper.writeValueAsString(build);
    mockMvc.perform(post("/rs/buy/"+rsEventDto.getId()).content(string).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    mockMvc
            .perform(get("/rs/list"))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
            .andExpect(jsonPath("$[0].keyword", is("健康")))
            .andExpect(jsonPath("$[0]", not(hasKey("user"))))
            .andExpect(status().isOk());
  }

  @Test
  void shouldGetRsEventByRankNumAfterTradeAndRankHasBought() throws Exception {
    UserDto save = userRepository.save(userDto);
    RsEventDto rsEventDto =
            RsEventDto.builder().keyword("健康").eventName("第一条事件").user(save).voteNum(12).rankNum(4).build();
    rsEventRepository.save(rsEventDto);
    RsEventDto rsEventDto1 =
            RsEventDto.builder().keyword("经济").eventName("猪肉涨价").user(save).voteNum(22).rankNum(2).build();

    rsEventRepository.save(rsEventDto1);

    RsEventDto rsEventDto2 =
            RsEventDto.builder().keyword("健康").eventName("锻炼身体").user(save).voteNum(30).rankNum(4).build();
    rsEventRepository.save(rsEventDto2);
    ObjectMapper objectMapper = new ObjectMapper();

    Trade build = Trade.builder().amount(100).rank(1).build();
    String string = objectMapper.writeValueAsString(build);
    mockMvc.perform(post("/rs/buy/"+rsEventDto.getId()).content(string).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    mockMvc
            .perform(get("/rs/list"))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
            .andExpect(jsonPath("$[0].keyword", is("健康")))
            .andExpect(jsonPath("$[0]", not(hasKey("user"))))
            .andExpect(status().isOk());


    Trade build2 = Trade.builder().amount(200).rank(1).build();
    String string2 = objectMapper.writeValueAsString(build2);
    mockMvc.perform(post("/rs/buy/"+rsEventDto1.getId()).content(string2).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

    mockMvc
            .perform(get("/rs/list"))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].eventName", is("猪肉涨价")))
            .andExpect(jsonPath("$[0].keyword", is("经济")))
            .andExpect(jsonPath("$[0]", not(hasKey("user"))))
            .andExpect(status().isOk());
  }

  @Test
  void shouldReturnBadGetRsEventByRankNumAfterTradeAndRankHasBought() throws Exception {
    UserDto save = userRepository.save(userDto);
    RsEventDto rsEventDto =
            RsEventDto.builder().keyword("健康").eventName("第一条事件").user(save).voteNum(12).rankNum(4).build();
    rsEventRepository.save(rsEventDto);
    RsEventDto rsEventDto1 =
            RsEventDto.builder().keyword("经济").eventName("猪肉涨价").user(save).voteNum(22).rankNum(2).build();

    rsEventRepository.save(rsEventDto1);

    RsEventDto rsEventDto2 =
            RsEventDto.builder().keyword("健康").eventName("锻炼身体").user(save).voteNum(30).rankNum(4).build();
    rsEventRepository.save(rsEventDto2);
    ObjectMapper objectMapper = new ObjectMapper();

    Trade build = Trade.builder().amount(100).rank(1).build();
    String string = objectMapper.writeValueAsString(build);
    mockMvc.perform(post("/rs/buy/"+rsEventDto.getId()).content(string).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    mockMvc
            .perform(get("/rs/list"))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
            .andExpect(jsonPath("$[0].keyword", is("健康")))
            .andExpect(jsonPath("$[0]", not(hasKey("user"))))
            .andExpect(status().isOk());


    Trade build2 = Trade.builder().amount(20).rank(1).build();
    String string2 = objectMapper.writeValueAsString(build2);
    mockMvc.perform(post("/rs/buy/"+rsEventDto1.getId()).content(string2).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }

}
