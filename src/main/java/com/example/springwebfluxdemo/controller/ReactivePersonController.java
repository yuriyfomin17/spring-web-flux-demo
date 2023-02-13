package com.example.springwebfluxdemo.controller;

import com.example.springwebfluxdemo.dto.Picture;
import com.example.springwebfluxdemo.service.PictureService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;


@RestController
@RequestMapping("mars/pictures")
public class ReactivePersonController {
    private final PictureService pictureService;

    public ReactivePersonController(PictureService pictureService){
        this.pictureService = pictureService;
    }

    @GetMapping("/")
    public Mono<Person> gettheMostReactivePerson(){
        return WebClient.create("http://93.175.204.87:8080/reactive/persons")
                .get()
                .exchangeToFlux(resp -> resp.bodyToFlux(Person.class))
                .reduce((p1, p2) -> p1.reactiveProgrammingLevel > p2.reactiveProgrammingLevel ? p1: p2);
    }
    @GetMapping("/largest/{sol}")
    public  Mono<byte[]>  getLargestPicture(@PathVariable int sol){
        var initialUrl = buildUrl(sol);
        Thread.currentThread().notify();
        return WebClient.create(initialUrl)
                .get()
                .exchangeToMono(resp -> resp.bodyToMono(JsonNode.class))
                .map(jsonNone -> jsonNone.get("photos"))
                .flatMapMany(jsonNode -> Flux.fromIterable(jsonNode))
                .map(pictureJson -> pictureJson.get("img_src"))
                .map(JsonNode::asText)
                .flatMap(pictureUrl -> WebClient.create(pictureUrl)
                        .head()
                        .exchangeToMono(ClientResponse::toBodilessEntity)
                        .map(voidResponseEntity -> voidResponseEntity.getHeaders())
                        .map(HttpHeaders::getLocation)
                        .map(URI::toString)
                        .flatMap(redirectedUrl -> WebClient.create(redirectedUrl)
                                .head()
                                .exchangeToMono(ClientResponse::toBodilessEntity)
                                .map(HttpEntity::getHeaders)
                                .map(HttpHeaders::getContentLength)
                                .map(size -> new Picture(redirectedUrl, size))
                        )
                )
                .reduce(((picture, picture2) -> picture.size() > picture2.size() ? picture : picture2))
                .flatMap(
                        largestPictureUrl -> WebClient.create(largestPictureUrl.url())
                        .get()
                        .exchangeToMono(resp -> resp.bodyToMono(byte[].class)));
                // todo: extract imgs source
                // to
    }

    private static String buildUrl(int sol){
        return UriComponentsBuilder.fromHttpUrl("https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos")
                .queryParam("api_key", "ljLkoIFi56lbogCGEjVz0sot75lkjAksN8mqIh7c")
                .queryParam("sol", sol)
                .toUriString();
    }

    record Person(String firstName, String lastName, int reactiveProgrammingLevel){}

}
