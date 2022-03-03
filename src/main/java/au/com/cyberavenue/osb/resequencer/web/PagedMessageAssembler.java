package au.com.cyberavenue.osb.resequencer.web;

import static org.springframework.web.util.UriComponentsBuilder.fromUri;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.UriTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import au.com.cyberavenue.osb.resequencer.entity.soainfra.MessageProjection;

public class PagedMessageAssembler extends PagedResourcesAssembler<MessageProjection> {

    private final HateoasPageableHandlerMethodArgumentResolver pageableResolver;

    public PagedMessageAssembler(HateoasPageableHandlerMethodArgumentResolver resolver,
            UriComponents baseUri) {
        super(resolver, baseUri);
        this.pageableResolver = resolver;
    }

    @Override
    public PagedModel<EntityModel<MessageProjection>> toModel(Page<MessageProjection> entity) {
        PagedModel<EntityModel<MessageProjection>> resources = super.toModel(entity);
        addSortingLinks(resources, entity, Optional.empty());
        return resources;
    }

    private PagedModel<EntityModel<MessageProjection>> addSortingLinks(
            PagedModel<EntityModel<MessageProjection>> resources,
            Page<MessageProjection> page,
            Optional<Link> link) {

        Link selfLink = resources.getRequiredLink(IanaLinkRelations.SELF);
        UriTemplate base = selfLink.getTemplate();

        Sort sort = page.getSort();
        String[] properties = {
                "creationDate",
                "componentDn",
                "operation",
                "groupId",
                "groupStatus",
                "messageStatus"
        };
        List<Link> sortLinks = Arrays.stream(properties)
                .map(p -> Optional.ofNullable(sort.getOrderFor(p)).orElse(Sort.Order.asc(p)))
                .map(o -> o.isAscending() ? o.with(Sort.Direction.DESC) : o.with(Sort.Direction.ASC))
                .map(o -> createLink(base, PageRequest.of(0, page.getSize(),
                        Sort.by(o)), LinkRelation.of(o.getProperty())))
                .collect(Collectors.toList());
        resources.add(sortLinks);

        return resources;
    }

    private Link createLink(UriTemplate base, Pageable pageable, LinkRelation relation) {

        UriComponentsBuilder builder = fromUri(base.expand());
        pageableResolver.enhance(builder, getMethodParameter(), pageable);

        return Link.of(UriTemplate.of(builder.build().toString()), relation);
    }

}
