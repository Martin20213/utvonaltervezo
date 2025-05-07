// app/src/main/java/com/example/utvonaltervezo/domain/usecase/GetRouteUseCase.java
package com.example.utvonaltervezo.domain.usecase;

// Domain réteg
public interface GetRouteUseCase {
    void execute(String startPoint, String endPoint, String travelMode);
}
