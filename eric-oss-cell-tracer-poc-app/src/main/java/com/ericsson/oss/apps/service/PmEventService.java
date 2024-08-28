/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.oss.apps.service;

import com.ericsson.oss.apps.model.entities.NrCellEvent;
import com.ericsson.oss.apps.model.entities.NrEvent;
import com.ericsson.oss.apps.model.entities.NrNodeEvent;
import com.ericsson.oss.apps.repository.NrCellPmEventRepository;
import com.ericsson.oss.apps.repository.NrNodePmEventRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
public class PmEventService {

    private final NrCellPmEventRepository cellRepo;
    private final NrNodePmEventRepository nodeRepo;
    private final EntityManager em;
    private final int maxEventsReturned;

    public PmEventService(NrCellPmEventRepository cellRepo,
                          NrNodePmEventRepository nodeRepo,
                          EntityManager em,
                          @Value("${app.data.events.maxEventsReturned}") int maxEventsReturned) {
        this.cellRepo = cellRepo;
        this.nodeRepo = nodeRepo;
        this.em = em;
        this.maxEventsReturned = maxEventsReturned;
    }


    public List<NrCellEvent> findAllCellEvent() {
        return cellRepo.findAll();
    }

    public List<NrNodeEvent> findAllNodeEvent() {
        return nodeRepo.findAll();
    }

    @Transactional
    public int batchInsertEvents(List<NrEvent> eventList) {
        AtomicInteger integer = new AtomicInteger();
        eventList.forEach(event -> {
            em.persist(event);
            integer.incrementAndGet();
        });
        return integer.get();
    }

    public List<NrEvent> findFilterEvents(EventFilters eventFilters) {
        return Stream.concat(findFilterEvents(NrCellEvent.class, eventFilters).stream(),
                        findFilterEvents(NrNodeEvent.class, eventFilters).stream())
                .sorted((e1, e2) -> Long.compare(e2.getTimeStamp(), e1.getTimeStamp()))
                .limit(maxEventsReturned).toList();
    }

    private <T extends NrEvent> List<T> findFilterEvents(Class<T> eventClass, EventFilters eventFilters) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cqEvent = cb.createQuery(eventClass);
        Root<T> root = cqEvent.from(eventClass);
        var predicates = getPredicates(eventFilters, cb, root);
        if (eventClass.equals(NrCellEvent.class)) {
            eventFilters.nci().stream().findFirst().ifPresent(x -> predicates.add(root.get(NrCellEvent.Fields.nCI).in(eventFilters.nci())));
        }
        return em.createQuery(cqEvent.select(root).where(cb.and(predicates.toArray(new Predicate[0]))))
                .setMaxResults(maxEventsReturned)
                .getResultList();
    }

    @NotNull
    private static ArrayList<Predicate> getPredicates(EventFilters eventFilters, CriteriaBuilder cb, Root<? extends NrEvent> root) {
        var predicates = new ArrayList<Predicate>();
        predicates.add(cb.ge(root.get(NrEvent.Fields.timeStamp), eventFilters.start()));
        predicates.add(cb.lt(root.get(NrEvent.Fields.timeStamp), eventFilters.end()));
        eventFilters.gnid().stream().findFirst().ifPresent(values -> predicates.add(root.get(NrEvent.Fields.gnbId).in(eventFilters.gnid())));
        eventFilters.eId().stream().findFirst().ifPresent(values -> predicates.add(root.get(NrEvent.Fields.eventId).in(values)));
        eventFilters.pmEventMessageGroupList().stream().findFirst().ifPresent(values -> predicates.add(root.get(NrEvent.Fields.msgGroup).in(eventFilters.pmEventMessageGroupList())));
        Optional.ofNullable(eventFilters.ueTraceId()).ifPresent(value -> predicates.add(cb.equal(root.get(NrEvent.Fields.ueTraceId), value)));
        Optional.ofNullable(eventFilters.traceRecordingSessionReference()).ifPresent(value -> predicates.add(cb.equal(root.get(NrEvent.Fields.traceRecordingSessionReference), value)));
        return predicates;
    }


}