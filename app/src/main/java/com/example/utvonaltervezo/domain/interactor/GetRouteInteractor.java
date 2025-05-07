
// app/src/main/java/com/example/utvonaltervezo/domain/interactor/GetRouteInteractor.java
package com.example.utvonaltervezo.domain.interactor;

import com.example.utvonaltervezo.domain.model.RouteResponse;
import com.example.utvonaltervezo.domain.usecase.GetRouteUseCase;
import com.example.utvonaltervezo.domain.repository.RouteRepository;
import com.example.utvonaltervezo.presentation.ui.RoutePresenter;

// Domain réteg
public class GetRouteInteractor implements GetRouteUseCase {
    private final RouteRepository routeRepository;
    private final RoutePresenter presenter;

    public GetRouteInteractor(RouteRepository routeRepository, RoutePresenter presenter) {
        this.routeRepository = routeRepository;
        this.presenter = presenter;
    }

    @Override
    public void execute(String startPoint, String endPoint, String travelMode) {
        routeRepository.getRoute(startPoint, endPoint, travelMode, new RouteRepository.RouteCallback() {
            @Override
            public void onSuccess(RouteResponse routeResponse) {
                presenter.presentRoute(routeResponse);
            }

            @Override
            public void onError(String errorMessage) {
                presenter.presentError(errorMessage);
            }
        });
    }
}
