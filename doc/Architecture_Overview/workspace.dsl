workspace {

    model {
        user = person "Network troubleshooter"

        cellTracer = softwareSystem "Cell Tracer rApp" {

            rappBackend = group "Cell Tracer Backend" {

                springBootBackend = container "Cell Tracer SpringBoot Backend" {
                    subscriptionService = component "Subscription Service" {
                        tags "Service"
                    }

                    topologyService = component "Topology Service" {
                        tags "Service"
                    }

                    cmService = component "CM Service" {
                        tags "Service"
                    }
                    dccClient = component "DCC client"
                    topologyClient = component "Topology Client"

                    eventStreamConsumer = component "CTR Event Consumer"
                    cmClient = component "NCMP Client"
                    eventService = component "Event Report Service" {
                        tags "Service"
                    }
                }

                asn1DecodingSidecar = container "ASN.1 Decoding sidecar" {
                    asn1DecodingService = component "ASN.1 Decoding service" {
                        tags "Service"
                    }
                }

                cellTracerDatabase = container "Cell Tracer database" {
                    tags "Database"
                }
            }


            owl = group "Cell Tracer rApp frontend" {

                owlFrontendService = container "OWL frontend" {
                    tags "Service"
                }

                owlBackendService = container "OWL backend" {
                    tags "Service"
                }

                owlDb = container "OWL data store (Redis)" {
                    tags "Database"
                }

            }

        }

        eic = softwareSystem "Ericsson Intelligent Controller" {
            dmm = group "DMM" {
                kafka = container "Kafka" {
                    tags "Bus"
                }
                dcc = container "DCC" {
                    tags "Service"
                }
            }

            tic = group "TIC" {
                ncmp = container "NCMP" {
                    tags "Service"
                }
                cts = container "CTS" {
                    tags "Service"
                }
            }

        }

        user -> owlFrontendService "[http over TLS] report on events, set up subscriptions"

        owlFrontendService -> owlBackendService "[http over TLS] report on events, set up subscriptions"
        owlBackendService -> subscriptionService "[REST, unsecured] set up subscription"
        owlBackendService -> eventService "[REST, unsecured] report on events"
        owlBackendService -> cmService "[REST, unsecured] retrieve GnodeB and cell list"
        owlBackendService -> topologyService "[REST, unsecured] retrieve available GnodeB list"
        owlBackendService -> owlDb "[REDIS, unsecured] cache/retrieve data"

        subscriptionService -> cmClient "[Java API] refresh CM information"
        cmClient -> ncmp "[REST, http over TLS] fetch CM information (nci, gNodebId)"
        cmClient -> cellTracerDatabase "[JDBC over TLS] cache CM data"
        cmService -> cellTracerDatabase "[JDBC over TLS] retrieve CM data"
        topologyService -> topologyClient "[Java API] refresh node list for subscription setup"
        subscriptionService -> cellTracerDatabase "[JDBC over TLS] store subscription"
        subscriptionService -> dccClient "[Java API] set up subscription"
        dccClient -> dcc "[REST, http over TLS] set up subscription"
        topologyClient -> cts "[REST http over TLS] collect all node list"

        eventStreamConsumer -> asn1DecodingService "[GRPC, unsecured] decode ASN.1"
        eventStreamConsumer -> kafka "[Kafka, unsecured] listen to cell trace events"
        eventStreamConsumer -> cellTracerDatabase "[JDBC over TLS] store events"
        eventStreamConsumer -> subscriptionService "[Java API] fetch filters"
        eventService -> cellTracerDatabase "[JDBC over TLS] read events"

    }

    views {

        systemContext cellTracer "SystemView" {
            include *
        }

        container cellTracer "CellTracerContainerView" {
            include *
        }

        container eic "EICContainerView" {
            include *
        }

        component springBootBackend "BackendComponentView" {
            include *
        }


        styles {
            element "Person" {
                shape person
                background #000082
                color #ffffff
            }
            element "SoftwareSystem" {
                background #000082
                color #ffffff
            }
            element "Container" {
                background #0050ca
                color #ffffff
            }
            element "Component" {
                background #1174e6
                color #ffffff
            }
            element "Service" {
                shape roundedBox
            }
            element "Database" {
                shape cylinder
            }
            element "Bus" {
                shape pipe
            }
        }
    }

}