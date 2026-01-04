package com.earn.earnmoney.dto;

import java.util.List;
import com.earn.earnmoney.model.UserAuth;
import com.earn.earnmoney.model.UserCounter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileResponse {

        private Long points;
        private int numberOfCounters;
        private List<UserCounterResponse> counters;
        private boolean banned;
        private String referralCode;
        private int numberOfReferral;

        public static UserProfileResponse from(
                        UserAuth user,
                        List<UserCounter> userCounters) {

                List<UserCounterResponse> counters = userCounters.stream()
                                .map(UserCounterResponse::from)
                                .toList();

                return new UserProfileResponse(
                                user.getPoints(),
                                counters.size(),
                                counters,
                                user.isBand(),
                                user.getReferralCode(),
                                user.getNumberOfReferral());
        }
}
