package com.example.springwebfluxdemo.service;

import com.example.springwebfluxdemo.dto.NasaPhotos;
import com.example.springwebfluxdemo.dto.Picture;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class PictureService {
    private final String NASA_URL = "https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos";
    private final String NASA_KEY = "ljLkoIFi56lbogCGEjVz0sot75lkjAksN8mqIh7c";

    public Mono<NasaPhotos> getLargestPictureFromSol(Long sol){
        return  WebClient.create(buildUri(sol))
                .get()
                .exchangeToFlux(resp -> resp.bodyToFlux(NasaPhotos.class))
                .reduce((nasaPhotos, nasaPhotos2) -> nasaPhotos.nasaPhotos().size() > nasaPhotos2.nasaPhotos().size() ? nasaPhotos: nasaPhotos2);


    }





    private String buildUri(Long sol){
        return UriComponentsBuilder.fromHttpUrl(NASA_URL)
                .queryParam("api_key", NASA_KEY)
                .queryParam("sol", sol)
                .build().toUri().toString();

    }

}
