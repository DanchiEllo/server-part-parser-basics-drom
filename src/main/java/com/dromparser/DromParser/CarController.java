package com.dromparser.DromParser;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
public class CarController {

    private CarService carService;

    @Autowired
    public CarController(CarService carService) {
        this.carService = carService;
    }


    @PostMapping("/carParse")
    public DeferredResult<ResponseEntity<List<Car>>> parseAndAddCars(@RequestBody Map<String, String> json) {
        String url = json.get("URL");
        CompletableFuture<List<Car>> allCars = carService.parseAndAddCarsAsync(url);

        DeferredResult<ResponseEntity<List<Car>>> result = new DeferredResult<>();

        allCars.thenAccept(cars -> {
            if (cars != null) {
                result.setResult(new ResponseEntity<>(cars, HttpStatus.CREATED));
            } else {
                result.setResult(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
            }
        }).exceptionally(ex -> {
            result.setResult(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
            return null;
        });

        return result;
    }

    @GetMapping("/allCars")
    public Iterable<Car> getAllCars() {
        return carService.getAllCars();
    }

}
