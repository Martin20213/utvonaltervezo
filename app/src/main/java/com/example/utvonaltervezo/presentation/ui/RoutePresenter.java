
// app/src/main/java/com/example/utvonaltervezo/presentation/ui/RoutePresenter.java
package com.example.utvonaltervezo.presentation.ui;

import com.example.utvonaltervezo.domain.model.RouteResponse;

import java.util.List;

// Presentation réteg
public interface RoutePresenter {
    void presentRoute(RouteResponse routeResponse);
    void presentError(String errorMessage);
}
