package io.github.lilaschuda.guanaco.demo.showdown.route;

import io.github.lilaschuda.guanaco.core.RouteOutcome;

public sealed interface ClaimsRoute<T> extends RouteOutcome 
    permits ToEmeaAutoSpeedlane, ToEmeaGeneralHighValue, ToEmeaArchive, 
            ToAmerFraudHold, ToAmerGeneral, ToGlobalTriage {
    T body();
}

record ToEmeaAutoSpeedlane(String body) implements ClaimsRoute<String> {}
record ToEmeaGeneralHighValue(String body) implements ClaimsRoute<String> {}
record ToEmeaArchive(String body) implements ClaimsRoute<String> {}
record ToAmerFraudHold(String body) implements ClaimsRoute<String> {}
record ToAmerGeneral(String body) implements ClaimsRoute<String> {}
record ToGlobalTriage(String body) implements ClaimsRoute<String> {}