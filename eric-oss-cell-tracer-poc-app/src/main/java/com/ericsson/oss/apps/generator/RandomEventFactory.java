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
package com.ericsson.oss.apps.generator;

import com.ericsson.oss.apps.events.NREventMethodCache;
import com.ericsson.oss.apps.loader.AllowedNodeMap;
import com.ericsson.oss.apps.loader.MessageGroupToClassMapper;
import com.ericsson.oss.apps.loader.NREventClassMap;
import com.ericsson.oss.apps.model.AllowedNode;
import com.ericsson.oss.apps.model.NREventClass;
import com.ericsson.pm_event.PmEventOuterClass;
import com.google.protobuf.ByteString;
import com.google.protobuf.MapField;
import com.google.protobuf.Message;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.ObjectCreationException;
import org.jeasy.random.ObjenesisObjectFactory;
import org.jeasy.random.api.RandomizerContext;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static com.ericsson.oss.apps.generator.EntityIdRandomizer.GNB_ID_LENGTH;
import static org.jeasy.random.FieldPredicates.named;

@Slf4j
public class RandomEventFactory extends ObjenesisObjectFactory {

    private static final AtomicReference<AllowedNode> NODE = new AtomicReference<>();

    private final Random rn = new SecureRandom();

    private final NREventMethodCache nrEventMethodCache;

    private EasyRandomParameters getParameters(RandomEventFactory eventFactory, int seed) {
        return new EasyRandomParameters()
                .seed(seed)
                .objectFactory(eventFactory)
                .excludeType(Predicate.isEqual(MapField.class))
                .excludeField(named("nci_"))
                .excludeField(named("msgContent_"))
                .randomize(named("gnbId_"), new EntityIdRandomizer(NODE))
                .randomize(named("gnbDuId_")
                        .or(named("gnbCuupFunctionId_"))
                        .or(named("cuupId_"))
                        .or(named("duId_")), () -> 1)
                .randomize(named("gnbIdLength_"), () -> GNB_ID_LENGTH)
                .randomize(named("timeStamp_"), System::currentTimeMillis)
                .randomize(named("msgDirection_"), new Asn1ContentRandomizer(eventClassMap.getEventClasses()));
    }


    private final NREventClassMap eventClassMap;
    private final AllowedNodeMap allowedNodeMap;
    private final EasyRandom payloadGenerator;
    private NREventClass[] nrEvents;
    private AllowedNode[] allowedNodes;

    public RandomEventFactory(NREventClassMap eventClassMap, AllowedNodeMap allowedNodeMap, NREventMethodCache nrEventMethodCache, int seed) {
        this.eventClassMap = eventClassMap;
        this.allowedNodeMap = allowedNodeMap;
        this.payloadGenerator = new EasyRandom(getParameters(this, seed));
        this.nrEventMethodCache = nrEventMethodCache;
    }

    private synchronized NREventClass[] getNrEvents() {
        if (nrEvents == null) {
            nrEvents = eventClassMap.getEventClasses().toArray(new NREventClass[0]);
        }
        return nrEvents.clone();
    }

    private synchronized AllowedNode[] getAllowedNodes() {
        if (allowedNodes == null) {
            allowedNodes = allowedNodeMap.getAllAllowedNodes().toArray(new AllowedNode[0]);
        }
        return allowedNodes.clone();
    }

    @SneakyThrows
    @Override
    public <T> T createInstance(Class<T> aClass, RandomizerContext randomizerContext) throws ObjectCreationException {
        if (PmEventOuterClass.PmEvent.class.isAssignableFrom(aClass)) {
            return (T) constructEvent();
        } else if (PmEventOuterClass.PmEventHeader.class.isAssignableFrom(aClass)) {
            return (T) constructHeader();
        }
        return super.createInstance(aClass, randomizerContext);
    }

    @SneakyThrows
    private PmEventOuterClass.PmEvent constructEvent() {
        PmEventOuterClass.PmEventHeader header = payloadGenerator.nextObject(PmEventOuterClass.PmEventHeader.class);
        header = PmEventOuterClass.PmEventHeader.newBuilder(header)
                .setUeTraceId(getRandomUeTraceId(11))
                .setTraceRecordingSessionReference(getRandomByteString()).build();
        NREventClass eventClass = getNrEvents()[payloadGenerator.nextInt(getNrEvents().length)];
        Message payload = payloadGenerator.nextObject(eventClass.eventClass());
        var groupInfo = MessageGroupToClassMapper.getGroupInfo(eventClass.pmEventMessageGroup());
        var setPayloadMethod = nrEventMethodCache.getPayloadSetterFromCache(groupInfo.builder().getClass(), eventClass.eventClass());
        setPayloadMethod.invoke(groupInfo.builder(), payload);
        var messageGroup = groupInfo.builder().build();
        return PmEventOuterClass.PmEvent.newBuilder()
                .setEventId(eventClass.eventId())
                .setHeader(header)
                .setPayload(messageGroup.toByteString())
                .setGroup(eventClass.pmEventMessageGroup())
                .setPmEventCommonVersion(6)
                .build();
    }

    private PmEventOuterClass.PmEventHeader constructHeader() {
        AllowedNode allowedNode = getAllowedNodes()[payloadGenerator.nextInt(0, getAllowedNodes().length)];
        NODE.set(allowedNode);

        return PmEventOuterClass.PmEventHeader.newBuilder()
                .setNetworkManagedElement(allowedNode.managedObjectId().toString())
                .setComputeName("RadioNode")
                .build();
    }

    private ByteString getRandomUeTraceId(int bound) {
        int nextId = rn.nextInt(bound);
        String nextTraceId = String.format("%08d", nextId);
        return ByteString.copyFromUtf8(nextTraceId);
    }

    private ByteString getRandomByteString() {
        byte[] b = new byte[10];
        rn.nextBytes(b);
        return ByteString.copyFrom(b);
    }
}
