package io.github.anthonydeseille.flairquery.demo.web;

import io.github.anthonydeseille.flairquery.demo.repository.PublisherRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** WITNESS: clean endpoint (DTO projection, PK lookup, one query). Must never be flagged. */
@RestController
@RequestMapping("/publishers")
public class PublisherController {

    private final PublisherRepository publishers;

    public PublisherController(PublisherRepository publishers) {
        this.publishers = publishers;
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<PublisherDto> get(@PathVariable long id) {
        return ResponseEntity.of(publishers.findDtoById(id));
    }
}
