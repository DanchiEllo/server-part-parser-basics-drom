package com.dromparser.DromParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;


@Service
public class CarService {

    private final CarRepository repository;
    private final ExecutorService executorService;

    public CarService(CarRepository repository) {
        this.repository = repository;
        this.executorService = Executors.newCachedThreadPool();
    }

    private String getCarBrand(Element car) {
        try {
            return car.selectFirst(ParserAttributeSettings.NAME_AND_YEAR).text().split("[,\\s]+")[0];
        } catch (Exception exception) {
            System.out.println("error getCarBrand");
            return null;
        }
    }

    private String getCarModel(Element car) {
        try {
            return car.selectFirst(ParserAttributeSettings.NAME_AND_YEAR).text().split("[,\\s]+")[1];
        } catch (Exception exception) {
            System.out.println("error getCarModel");
            return null;
        }
    }

    private Integer getCarYear(Element car) {
        try {
            return Integer.valueOf(car.selectFirst(ParserAttributeSettings.NAME_AND_YEAR).text().split("[,\\s]+")[2]);
        } catch (Exception exception) {
            System.out.println("error getCarYear");
            return null;
        }
    }

    private Integer getCarPrice(Element car) {
        try {
            return Integer.valueOf(car.selectFirst(ParserAttributeSettings.PRICE).text().replaceAll(" ", ""));
        } catch (Exception exception) {
            System.out.println("error getCarPrice");
            return null;
        }
    }

    private Integer getCarMileage(Element car) {
        try {
            int mileage = 0;
            Elements characteristics = car.select(ParserAttributeSettings.CHARACTERISTICS);
            for (Element characteristic : characteristics) {
                if (characteristic.text().contains("км")) {
                    mileage = Integer.parseInt(characteristic.text().replace(" км", "").replace(" ", ""));
                }
            }
            return mileage;
        } catch (Exception exception) {
            System.out.println("error getCarMileage");
            return null;
        }
    }

    private Float getCarEngineCapacity(Element car) {
        try {
            return Float.valueOf(car.selectFirst(ParserAttributeSettings.CHARACTERISTICS).text().split(" л")[0]);
        } catch (Exception exception) {
            System.out.println("error getCarEngineCapacity");
            return null;
        }
    }

    private Integer getCarHorsePower(Element car) {
        try {
            return Integer.valueOf(car.selectFirst(ParserAttributeSettings.CHARACTERISTICS).text().split("\\(")[1].replace(" л.с.),", ""));
        } catch (Exception exception) {
            System.out.println("error getCarHorsePower");
            return null;
        }
    }

    private String getCarLocation(Element car) {
        try {
            return car.selectFirst(ParserAttributeSettings.LOCATION).text().trim();
        } catch (Exception exception) {
            System.out.println("error getCarLocation");
            return null;
        }
    }

    private String getCarImageUrl(Element car) {
        try {
            return car.selectFirst(ParserAttributeSettings.IMAGE).attr("data-srcset").split(" 1x")[0];
        } catch (Exception exception) {
            System.out.println("error getCarImageUrl");
            return null;
        }
    }

    private LocalDateTime getCarPublicationTime(Element car) {
        try {
            String text = car.selectFirst(ParserAttributeSettings.PUBLICATION_TIME).text();
            LocalDateTime publicationTime = null;
            if (text.contains("минут")) {
                int minutes = Integer.parseInt(text.replaceAll("[^\\d]", ""));
                publicationTime = LocalDateTime.now().minus(Duration.ofMinutes(minutes));
            } else if (text.contains("час")) {
                publicationTime = LocalDateTime.now().minus(Duration.ofHours(1));
            } else {
                int currentYear = LocalDate.now().getYear();
                String stringWithYear = text + " " + currentYear;

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("ru"));
                LocalDate date = LocalDate.parse(stringWithYear, formatter);
                publicationTime = LocalDateTime.of(date, LocalTime.MIDNIGHT);
            }

            return publicationTime;
        } catch (Exception exception) {
            System.out.println("error getCarPublicationTime");
            return null;
        }
    }

    private String getCarURL(Element car) {
        try {
            String carURL = car.attr("href");

            return carURL;
        } catch (Exception exception) {
            System.out.println("error getCarURL");
            return null;
        }
    }

    public Iterable<Car> getAllCars() {
        return repository.findAll();
    }


    public CompletableFuture<List<Car>> parseAndAddCarsAsync(String url) {
        return CompletableFuture.supplyAsync(() -> parseAndAddCars(url), executorService);
    }

    public List<Car> parseAndAddCars(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .referrer("https://www.yandex.ru/")
                    .timeout(10000)
                    .get();

            ArrayList<Car> allCars = new ArrayList<>();

            Elements carNames = doc.select(ParserAttributeSettings.ONE_CAR);
            for (Element oneCar : carNames) {
                String carBrand = getCarBrand(oneCar);
                String carModel = getCarModel(oneCar);
                Integer carYear = getCarYear(oneCar);
                Integer carPrice = getCarPrice(oneCar);
                Integer carMileage = getCarMileage(oneCar);
                Float carEngineCapacity = getCarEngineCapacity(oneCar);
                Integer carHorsePower = getCarHorsePower(oneCar);
                String carLocation = getCarLocation(oneCar);
                String carImageUrl = getCarImageUrl(oneCar);
                LocalDateTime carPublicationTime = getCarPublicationTime(oneCar);
                String carURL = getCarURL(oneCar);

                allCars.add(new Car(carBrand, carModel, carYear,
                        carPrice, carMileage, carEngineCapacity,
                        carHorsePower, carLocation, carImageUrl,
                        carPublicationTime, carURL));

            }

            List<Car> carsToSave = new ArrayList<>();

            for (Car car : allCars) {
                if (!repository.existsByURL(car.getURL())) {
                    carsToSave.add(car);
                }
            }
            repository.saveAll(carsToSave);

            return allCars;

        } catch (Exception exception) {
            System.out.println("error carParser");
            return null;
        }
    }
}
