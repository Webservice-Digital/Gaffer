/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.gaffer.federatedstore;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.gchq.gaffer.accumulostore.SingleUseAccumuloStore;
import uk.gov.gchq.gaffer.commonutil.StreamUtil;
import uk.gov.gchq.gaffer.commonutil.iterable.CloseableIterable;
import uk.gov.gchq.gaffer.commonutil.pair.Pair;
import uk.gov.gchq.gaffer.data.element.Edge;
import uk.gov.gchq.gaffer.data.element.Element;
import uk.gov.gchq.gaffer.data.elementdefinition.view.View;
import uk.gov.gchq.gaffer.federatedstore.integration.FederatedStoreITs;
import uk.gov.gchq.gaffer.graph.Graph;
import uk.gov.gchq.gaffer.mapstore.MapStore;
import uk.gov.gchq.gaffer.mapstore.MapStoreProperties;
import uk.gov.gchq.gaffer.operation.impl.add.AddElements;
import uk.gov.gchq.gaffer.operation.impl.get.GetAllElements;
import uk.gov.gchq.gaffer.store.StoreProperties;
import uk.gov.gchq.gaffer.store.StoreTrait;
import uk.gov.gchq.gaffer.store.library.GraphLibrary;
import uk.gov.gchq.gaffer.store.library.HashMapGraphLibrary;
import uk.gov.gchq.gaffer.store.schema.Schema;
import uk.gov.gchq.gaffer.store.schema.Schema.Builder;
import uk.gov.gchq.gaffer.user.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static uk.gov.gchq.gaffer.federatedstore.FederatedStore.S1_WAS_NOT_ABLE_TO_BE_CREATED_WITH_THE_SUPPLIED_PROPERTIES_GRAPH_ID_S2;
import static uk.gov.gchq.gaffer.federatedstore.FederatedStore.USER_IS_ATTEMPTING_TO_OVERWRITE_A_GRAPH_WITHIN_FEDERATED_STORE_GRAPH_ID_S;

public class FederatedStoreTest {
    public static final String PATH_FEDERATED_STORE_PROPERTIES = "/properties/federatedStoreTest.properties";
    public static final String FEDERATED_STORE_ID = "testFederatedStoreId";
    public static final String ACC_ID_1 = "mockAccGraphId1";
    public static final String MAP_ID_1 = "mockMapGraphId1";
    public static final String PATH_ACC_STORE_PROPERTIES = "properties/singleUseMockAccStore.properties";
    public static final String PATH_MAP_STORE_PROPERTIES = "properties/singleUseMockMapStore.properties";
    public static final String PATH_MAP_STORE_PROPERTIES_ALT = "properties/singleUseMockMapStoreAlt.properties";
    public static final String PATH_BASIC_ENTITY_SCHEMA_JSON = "schema/basicEntitySchema.json";
    public static final String PATH_BASIC_EDGE_SCHEMA_JSON = "schema/basicEdgeSchema.json";
    public static final String KEY_ACC_ID1_PROPERTIES_FILE = "gaffer.federatedstore.mockAccGraphId1.properties.file";
    public static final String KEY_MAP_ID1_PROPERTIES_FILE = "gaffer.federatedstore.mockMapGraphId1.properties.file";
    public static final String KEY_MAP_ID1_PROPERTIES_ID = "gaffer.federatedstore.mockMapGraphId1.properties.id";
    public static final String GRAPH_IDS = "gaffer.federatedstore.graphIds";
    public static final String KEY_ACC_ID1_SCHEMA_FILE = "gaffer.federatedstore.mockAccGraphId1.schema.file";
    public static final String KEY_MAP_ID1_SCHEMA_FILE = "gaffer.federatedstore.mockMapGraphId1.schema.file";
    public static final String KEY_MAP_ID1_SCHEMA_ID = "gaffer.federatedstore.mockMapGraphId1.schema.id";
    public static final String PATH_INVALID = "nothing.json";
    public static final String EXCEPTION_NOT_THROWN = "exception not thrown";
    public static final User TEST_USER = new User("testUser");
    public static final String PROPS_ID_1 = "PROPS_ID_1";
    public static final String SCHEMA_ID_1 = "SCHEMA_ID_1";
    FederatedStore store;
    private StoreProperties federatedProperties;

    @Before
    public void setUp() throws Exception {
        store = new FederatedStore();
        federatedProperties = new StoreProperties();
    }

    @Test
    public void shouldLoadGraphsWithIds() throws Exception {
        //Given
        federatedProperties.set(GRAPH_IDS, ACC_ID_1 + "," + MAP_ID_1);
        federatedProperties.set(KEY_ACC_ID1_PROPERTIES_FILE, PATH_ACC_STORE_PROPERTIES);
        federatedProperties.set(KEY_ACC_ID1_SCHEMA_FILE, PATH_BASIC_ENTITY_SCHEMA_JSON);
        federatedProperties.set(KEY_MAP_ID1_PROPERTIES_FILE, PATH_MAP_STORE_PROPERTIES);
        federatedProperties.set(KEY_MAP_ID1_SCHEMA_FILE, PATH_BASIC_EDGE_SCHEMA_JSON);

        //Then
        int before = store.getGraphs().size();

        //When
        store.initialise(FEDERATED_STORE_ID, federatedProperties);

        //Then
        Collection<Graph> graphs = store.getGraphs();
        int after = graphs.size();
        assertEquals(0, before);
        assertEquals(2, after);
        ArrayList<String> graphNames = Lists.newArrayList(ACC_ID_1, MAP_ID_1);
        for (Graph graph : graphs) {
            assertTrue(graphNames.contains(graph.getGraphId()));
        }
    }

    @Test
    public void shouldThrowErrorForFailedSchema() throws Exception {
        //Given
        federatedProperties.set(GRAPH_IDS, MAP_ID_1);
        federatedProperties.set(KEY_MAP_ID1_PROPERTIES_FILE, PATH_MAP_STORE_PROPERTIES);
        federatedProperties.set(KEY_MAP_ID1_SCHEMA_FILE, PATH_INVALID);

        //When
        try {
            store.initialise(FEDERATED_STORE_ID, federatedProperties);
        } catch (final IllegalArgumentException e) {
            //Then
            assertEquals(String.format(S1_WAS_NOT_ABLE_TO_BE_CREATED_WITH_THE_SUPPLIED_PROPERTIES_GRAPH_ID_S2, "Schema", "graphId: " + MAP_ID_1 + " schemaPath: " + PATH_INVALID), e.getMessage());
            return;
        }
        fail(EXCEPTION_NOT_THROWN);
    }

    @Test
    public void shouldThrowErrorForFailedProperty() throws Exception {
        //Given
        federatedProperties.set(GRAPH_IDS, MAP_ID_1);
        federatedProperties.set(KEY_MAP_ID1_PROPERTIES_FILE, PATH_INVALID);
        federatedProperties.set(KEY_MAP_ID1_SCHEMA_FILE, PATH_BASIC_EDGE_SCHEMA_JSON);

        //When
        try {
            store.initialise(FEDERATED_STORE_ID, federatedProperties);
        } catch (final IllegalArgumentException e) {
//            Then
            assertEquals(String.format(S1_WAS_NOT_ABLE_TO_BE_CREATED_WITH_THE_SUPPLIED_PROPERTIES_GRAPH_ID_S2, "Property", "graphId: " + MAP_ID_1 + " propertyPath: " + PATH_INVALID), e.getMessage());
            return;
        }
        fail(EXCEPTION_NOT_THROWN);
    }

    @Test
    public void shouldThrowErrorForIncompleteBuilder() throws Exception {
        //Given
        federatedProperties.set(GRAPH_IDS, MAP_ID_1);

        //When
        try {
            store.initialise(FEDERATED_STORE_ID, federatedProperties);
        } catch (final IllegalArgumentException e) {
            //Then
            assertTrue(e.getMessage().contains(String.format(S1_WAS_NOT_ABLE_TO_BE_CREATED_WITH_THE_SUPPLIED_PROPERTIES_GRAPH_ID_S2, "Graph", "Graph.Builder[config=GraphConfig[graphId=mockMapGraphId1,library=uk.gov.gchq.gaffer.store.library.NoGraphLibrary")));
            return;
        }
        fail(EXCEPTION_NOT_THROWN);
    }

    @Test
    public void shouldNotAllowOverwritingOfGraphWithFederatedScope() throws Exception {
        //Given
        federatedProperties.set(GRAPH_IDS, ACC_ID_1);
        federatedProperties.set(KEY_ACC_ID1_PROPERTIES_FILE, PATH_ACC_STORE_PROPERTIES);
        federatedProperties.set(KEY_ACC_ID1_SCHEMA_FILE, PATH_BASIC_ENTITY_SCHEMA_JSON);

        store.initialise(FEDERATED_STORE_ID, federatedProperties);

        try {
            store.addGraphs(new Graph.Builder()
                    .graphId(ACC_ID_1)
                    .storeProperties(StreamUtil.openStream(FederatedStoreTest.class, PATH_ACC_STORE_PROPERTIES))
                    .addSchema(StreamUtil.openStream(FederatedStoreTest.class, PATH_BASIC_EDGE_SCHEMA_JSON))
                    .build());
        } catch (final Exception e) {
            assertEquals(String.format(USER_IS_ATTEMPTING_TO_OVERWRITE_A_GRAPH_WITHIN_FEDERATED_STORE_GRAPH_ID_S, ACC_ID_1), e.getMessage());
            return;
        }
        fail(EXCEPTION_NOT_THROWN);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldDoUnhandledOperation() throws Exception {
        store.doUnhandledOperation(null, null);
    }

    @Test
    public void shouldUpdateTraitsWhenNewGraphIsAdded() throws Exception {
        federatedProperties.set(GRAPH_IDS, ACC_ID_1);
        federatedProperties.set(KEY_ACC_ID1_PROPERTIES_FILE, PATH_ACC_STORE_PROPERTIES);
        federatedProperties.set(KEY_ACC_ID1_SCHEMA_FILE, PATH_BASIC_ENTITY_SCHEMA_JSON);

        store.initialise(FEDERATED_STORE_ID, federatedProperties);

        //With less Traits
        Set<StoreTrait> before = store.getTraits();

        store.addGraphs(new Graph.Builder()
                .graphId(MAP_ID_1)
                .storeProperties(StreamUtil.openStream(FederatedStoreTest.class, PATH_MAP_STORE_PROPERTIES))
                .addSchema(StreamUtil.openStream(FederatedStoreTest.class, PATH_BASIC_ENTITY_SCHEMA_JSON))
                .build());

        //includes same as before but with more Traits
        Set<StoreTrait> after = store.getTraits();
        assertEquals("Sole graph has 9 traits, so all traits of the federatedStore is 9", 9, before.size());
        assertEquals("the two graphs share 5 traits", 5, after.size());
        assertNotEquals(before, after);
        assertTrue(before.size() > after.size());
    }

    @Test
    public void shouldUpdateSchemaWhenNewGraphIsAdded() throws Exception {
        federatedProperties.set(GRAPH_IDS, ACC_ID_1);
        federatedProperties.set(KEY_ACC_ID1_PROPERTIES_FILE, PATH_ACC_STORE_PROPERTIES);
        federatedProperties.set(KEY_ACC_ID1_SCHEMA_FILE, PATH_BASIC_ENTITY_SCHEMA_JSON);

        store.initialise(FEDERATED_STORE_ID, federatedProperties);

        Schema before = store.getSchema();

        store.addGraphs(new Graph.Builder()
                .graphId(MAP_ID_1)
                .storeProperties(StreamUtil.openStream(FederatedStoreTest.class, PATH_MAP_STORE_PROPERTIES))
                .addSchema(StreamUtil.openStream(FederatedStoreTest.class, PATH_BASIC_EDGE_SCHEMA_JSON))
                .build());

        Schema after = store.getSchema();
        assertNotEquals(before, after);
    }

    @Test
    public void shouldUpdateTraitsToMinWhenGraphIsRemoved() throws Exception {
        federatedProperties.set(GRAPH_IDS, MAP_ID_1 + "," + ACC_ID_1);
        federatedProperties.set(KEY_MAP_ID1_PROPERTIES_FILE, PATH_MAP_STORE_PROPERTIES);
        federatedProperties.set(KEY_MAP_ID1_SCHEMA_FILE, PATH_BASIC_EDGE_SCHEMA_JSON);
        federatedProperties.set(KEY_ACC_ID1_PROPERTIES_FILE, PATH_ACC_STORE_PROPERTIES);
        federatedProperties.set(KEY_ACC_ID1_SCHEMA_FILE, PATH_BASIC_ENTITY_SCHEMA_JSON);

        store.initialise(FEDERATED_STORE_ID, federatedProperties);

        //With less Traits
        Set<StoreTrait> before = store.getTraits();
        store.remove(MAP_ID_1);
        Set<StoreTrait> after = store.getTraits();

        //includes same as before but with more Traits
        assertEquals("Shared traits between the two graphs should be " + 5, 5, before.size());
        assertEquals("Shared traits counter-intuitively will go up after removing graph, because the sole remaining graph has 9 traits", 9, after.size());
        assertNotEquals(before, after);
        assertTrue(before.size() < after.size());
    }

    @Test
    public void shouldUpdateSchemaWhenNewGraphIsRemoved() throws Exception {
        federatedProperties.set(GRAPH_IDS, MAP_ID_1 + "," + ACC_ID_1);
        federatedProperties.set(KEY_MAP_ID1_PROPERTIES_FILE, PATH_MAP_STORE_PROPERTIES);
        federatedProperties.set(KEY_MAP_ID1_SCHEMA_FILE, PATH_BASIC_EDGE_SCHEMA_JSON);
        federatedProperties.set(KEY_ACC_ID1_PROPERTIES_FILE, PATH_ACC_STORE_PROPERTIES);
        federatedProperties.set(KEY_ACC_ID1_SCHEMA_FILE, PATH_BASIC_ENTITY_SCHEMA_JSON);

        store.initialise(FEDERATED_STORE_ID, federatedProperties);

        Schema before = store.getSchema();

        store.remove(MAP_ID_1);

        Schema after = store.getSchema();
        assertNotEquals(before, after);
    }

    @Test
    public void shouldFailWithIncompleteSchema() throws Exception {
        //Given
        federatedProperties.set(GRAPH_IDS, ACC_ID_1);
        federatedProperties.set(KEY_ACC_ID1_PROPERTIES_FILE, PATH_ACC_STORE_PROPERTIES);
        federatedProperties.set(KEY_ACC_ID1_SCHEMA_FILE, "/schema/edgeX2NoTypesSchema.json");


        try {
            store.initialise(FEDERATED_STORE_ID, federatedProperties);
        } catch (final Exception e) {
            assertTrue(e.getMessage().contains(String.format(S1_WAS_NOT_ABLE_TO_BE_CREATED_WITH_THE_SUPPLIED_PROPERTIES_GRAPH_ID_S2, "Graph", "Graph.Builder[config=GraphConfig[graphId=mockAccGraphId1,library=uk.gov.gchq.gaffer.store.library.NoGraphLibrary")));
            return;
        }
        fail(EXCEPTION_NOT_THROWN);
    }

    @Test
    public void shouldTakeCompleteSchemaFromTwoFiles() throws Exception {
        //Given
        federatedProperties.set(GRAPH_IDS, ACC_ID_1);
        federatedProperties.set(KEY_ACC_ID1_PROPERTIES_FILE, PATH_ACC_STORE_PROPERTIES);
        federatedProperties.set(KEY_ACC_ID1_SCHEMA_FILE, "/schema/edgeX2NoTypesSchema.json" + ", /schema/edgeTypeSchema.json");


        int before = store.getGraphs().size();
        store.initialise(FEDERATED_STORE_ID, federatedProperties);
        int after = store.getGraphs().size();

        assertEquals(0, before);
        assertEquals(1, after);
    }

    @Test
    public void shouldAddTwoGraphs() throws Exception {
        federatedProperties = StoreProperties.loadStoreProperties(StreamUtil.openStream(FederatedStoreITs.class, PATH_FEDERATED_STORE_PROPERTIES));
        // When
        int sizeBefore = store.getGraphs().size();
        store.initialise(FEDERATED_STORE_ID, federatedProperties);
        int sizeAfter = store.getGraphs().size();

        //Then
        assertEquals(0, sizeBefore);
        assertEquals(2, sizeAfter);
    }

    @Test
    public void shouldCombineTraitsToMin() throws Exception {
        federatedProperties = StoreProperties.loadStoreProperties(StreamUtil.openStream(FederatedStoreITs.class, PATH_FEDERATED_STORE_PROPERTIES));
        //Given
        HashSet<StoreTrait> traits = new HashSet<>();
        traits.addAll(SingleUseAccumuloStore.TRAITS);
        traits.retainAll(MapStore.TRAITS);

        //When
        Set<StoreTrait> before = store.getTraits();
        int sizeBefore = before.size();
        store.initialise(FEDERATED_STORE_ID, federatedProperties);
        Set<StoreTrait> after = store.getTraits();
        int sizeAfter = after.size();

        //Then
        assertEquals(5, MapStore.TRAITS.size());
        assertEquals(9, SingleUseAccumuloStore.TRAITS.size());
        assertNotEquals(SingleUseAccumuloStore.TRAITS, MapStore.TRAITS);
        assertEquals(0, sizeBefore);
        assertEquals(5, sizeAfter);
        assertEquals(traits, after);
    }

    @Test
    public void shouldContainNoElements() throws Exception {
        federatedProperties = StoreProperties.loadStoreProperties(StreamUtil.openStream(FederatedStoreITs.class, PATH_FEDERATED_STORE_PROPERTIES));
        store.initialise(FEDERATED_STORE_ID, federatedProperties);
        Set<Element> after = getElements();
        assertEquals(0, after.size());
    }

    @Test
    public void shouldAddEdgesToOneGraph() throws Exception {
        federatedProperties = StoreProperties.loadStoreProperties(StreamUtil.openStream(FederatedStoreITs.class, PATH_FEDERATED_STORE_PROPERTIES));
        store.initialise(FEDERATED_STORE_ID, federatedProperties);

        AddElements op = new AddElements.Builder()
                .input(new Edge.Builder()
                        .group("BasicEdge")
                        .source("testSource")
                        .dest("testDest")
                        .property("property1", 12)
                        .build())
                .build();

        store.execute(op, TEST_USER);

        assertEquals(1, getElements().size());
    }

    @Test
    public void shouldReturnGraphIds() throws Exception {
        //Given
        federatedProperties.set(GRAPH_IDS, MAP_ID_1 + "," + ACC_ID_1);
        federatedProperties.set(KEY_ACC_ID1_PROPERTIES_FILE, PATH_ACC_STORE_PROPERTIES);
        federatedProperties.set(KEY_ACC_ID1_SCHEMA_FILE, PATH_BASIC_ENTITY_SCHEMA_JSON);
        federatedProperties.set(KEY_MAP_ID1_PROPERTIES_FILE, PATH_MAP_STORE_PROPERTIES);
        federatedProperties.set(KEY_MAP_ID1_SCHEMA_FILE, PATH_BASIC_EDGE_SCHEMA_JSON);
        store.initialise(FEDERATED_STORE_ID, federatedProperties);

        Set<String> allGraphIds = store.getAllGraphIds();

        assertEquals(2, allGraphIds.size());
        assertTrue(allGraphIds.contains(ACC_ID_1));
        assertTrue(allGraphIds.contains(MAP_ID_1));

    }

    @Test
    public void shouldUpdateGraphIds() throws Exception {
        //Given
        federatedProperties.set(GRAPH_IDS, ACC_ID_1);
        federatedProperties.set(KEY_ACC_ID1_PROPERTIES_FILE, PATH_ACC_STORE_PROPERTIES);
        federatedProperties.set(KEY_ACC_ID1_SCHEMA_FILE, PATH_BASIC_ENTITY_SCHEMA_JSON);
        store.initialise(FEDERATED_STORE_ID, federatedProperties);

        Set<String> allGraphId = store.getAllGraphIds();

        assertEquals(1, allGraphId.size());
        assertTrue(allGraphId.contains(ACC_ID_1));
        assertFalse(allGraphId.contains(MAP_ID_1));

        store.addGraphs(new Graph.Builder()
                .graphId(MAP_ID_1)
                .storeProperties(StreamUtil.openStream(FederatedStoreTest.class, PATH_MAP_STORE_PROPERTIES))
                .addSchema(StreamUtil.openStream(FederatedStoreTest.class, PATH_BASIC_ENTITY_SCHEMA_JSON))
                .build());


        Set<String> allGraphId2 = store.getAllGraphIds();

        assertEquals(2, allGraphId2.size());
        assertTrue(allGraphId2.contains(ACC_ID_1));
        assertTrue(allGraphId2.contains(MAP_ID_1));

        store.remove(ACC_ID_1);

        Set<String> allGraphId3 = store.getAllGraphIds();

        assertEquals(1, allGraphId3.size());
        assertFalse(allGraphId3.contains(ACC_ID_1));
        assertTrue(allGraphId3.contains(MAP_ID_1));

    }

    private Set<Element> getElements() throws uk.gov.gchq.gaffer.operation.OperationException {
        CloseableIterable<? extends Element> elements = store
                .execute(new GetAllElements.Builder()
                        .view(new View.Builder()
                                .edges(store.getSchema().getEdgeGroups())
                                .entities(store.getSchema().getEntityGroups())
                                .build())
                        .build(), TEST_USER);

        return Sets.newHashSet(elements);
    }

    @Test
    public void shouldNotUseSchema() throws Exception {
        federatedProperties.set(GRAPH_IDS, MAP_ID_1);
        federatedProperties.set(KEY_MAP_ID1_PROPERTIES_FILE, PATH_MAP_STORE_PROPERTIES);
        federatedProperties.set(KEY_MAP_ID1_SCHEMA_FILE, PATH_BASIC_EDGE_SCHEMA_JSON);
        final Schema unusedMock = Mockito.mock(Schema.class);
        Mockito.verifyNoMoreInteractions(unusedMock);

        store.initialise(FEDERATED_STORE_ID, unusedMock, federatedProperties);
    }

    @Test
    public void shouldAddGraphFromLibrary() throws Exception {
        //Given
        final GraphLibrary mockLibrary = Mockito.mock(GraphLibrary.class);
        Mockito.when(mockLibrary.get(MAP_ID_1)).thenReturn(
                new Pair<>(
                        new Schema.Builder()
                                .json(StreamUtil.openStream(this.getClass(), PATH_BASIC_ENTITY_SCHEMA_JSON))
                                .build(),
                        StoreProperties.loadStoreProperties(StreamUtil.openStream(this.getClass(), PATH_MAP_STORE_PROPERTIES))
                ));

        store.initialise(FEDERATED_STORE_ID, federatedProperties, mockLibrary);

        //When
        final int before = store.getGraphs().size();
        store.addGraphs(MAP_ID_1);
        final int after = store.getGraphs().size();
        //Then
        assertEquals(0, before);
        assertEquals(1, after);
    }

    @Test
    public void shouldAddNamedGraphFromGraphIDKeyButDefinedInLibrary() throws Exception {
        //Given
        federatedProperties.set(GRAPH_IDS, MAP_ID_1);
        federatedProperties.set(GRAPH_IDS, MAP_ID_1);
        final GraphLibrary mockLibrary = Mockito.mock(GraphLibrary.class);
        Mockito.when(mockLibrary.get(MAP_ID_1)).thenReturn(
                new Pair<>(
                        new Schema.Builder()
                                .json(StreamUtil.openStream(this.getClass(), PATH_BASIC_ENTITY_SCHEMA_JSON))
                                .build(),
                        StoreProperties.loadStoreProperties(StreamUtil.openStream(this.getClass(), PATH_MAP_STORE_PROPERTIES))
                ));

        store.initialise(FEDERATED_STORE_ID, federatedProperties, mockLibrary);

        //Then
        final int after = store.getGraphs().size();
        assertEquals(1, after);
    }

    @Test
    public void shouldAddGraphFromGraphIDKeyButDefinedProperties() throws Exception {
        //Given
        federatedProperties.set(GRAPH_IDS, MAP_ID_1);
        federatedProperties.set(KEY_MAP_ID1_PROPERTIES_FILE, PATH_ACC_STORE_PROPERTIES);
        federatedProperties.set(KEY_MAP_ID1_SCHEMA_FILE, PATH_BASIC_ENTITY_SCHEMA_JSON);

        final GraphLibrary mockLibrary = Mockito.mock(GraphLibrary.class);

        store.initialise(FEDERATED_STORE_ID, federatedProperties, mockLibrary);

        //Then
        final int after = store.getGraphs().size();
        assertEquals(1, after);
    }

    @Test
    public void shouldAddNamedGraphsFromGraphIDKeyButDefinedInLibraryAndProperties() throws Exception {
        //Given
        federatedProperties.set(GRAPH_IDS, MAP_ID_1 + ", " + ACC_ID_1);
        federatedProperties.set(KEY_ACC_ID1_PROPERTIES_FILE, PATH_ACC_STORE_PROPERTIES);
        federatedProperties.set(KEY_ACC_ID1_SCHEMA_FILE, PATH_BASIC_ENTITY_SCHEMA_JSON);
        final GraphLibrary mockLibrary = Mockito.mock(GraphLibrary.class);
        Mockito.when(mockLibrary.get(MAP_ID_1)).thenReturn(
                new Pair<>(
                        new Schema.Builder()
                                .json(StreamUtil.openStream(this.getClass(), PATH_BASIC_ENTITY_SCHEMA_JSON))
                                .build(),
                        StoreProperties.loadStoreProperties(StreamUtil.openStream(this.getClass(), PATH_MAP_STORE_PROPERTIES))
                ));

        store.initialise(FEDERATED_STORE_ID, federatedProperties, mockLibrary);

        //Then
        final int after = store.getGraphs().size();
        assertEquals(2, after);
    }

    @Test
    public void shouldAddGraphWithPropertiesFromGraphLibrary() throws Exception {
        federatedProperties.set(GRAPH_IDS, MAP_ID_1);
        federatedProperties.set(KEY_MAP_ID1_PROPERTIES_ID, PROPS_ID_1);
        federatedProperties.set(KEY_MAP_ID1_SCHEMA_FILE, PATH_BASIC_EDGE_SCHEMA_JSON);
        final GraphLibrary mockLibrary = Mockito.mock(GraphLibrary.class);
        Mockito.when(mockLibrary.getProperties(PROPS_ID_1)).thenReturn(StoreProperties.loadStoreProperties(PATH_MAP_STORE_PROPERTIES));

        store.initialise(FEDERATED_STORE_ID, federatedProperties, mockLibrary);

        assertEquals(1, store.getGraphs().size());

        Mockito.verify(mockLibrary).getProperties(PROPS_ID_1);
    }

    @Test
    public void shouldAddGraphWithSchemaFromGraphLibrary() throws Exception {
        federatedProperties.set(GRAPH_IDS, MAP_ID_1);
        federatedProperties.set(KEY_MAP_ID1_PROPERTIES_FILE, PATH_MAP_STORE_PROPERTIES);
        federatedProperties.set(KEY_MAP_ID1_SCHEMA_ID, SCHEMA_ID_1);
        final GraphLibrary mockLibrary = Mockito.mock(GraphLibrary.class);
        Mockito.when(mockLibrary.getSchema(SCHEMA_ID_1))
                .thenReturn(new Schema.Builder()
                        .json(StreamUtil.openStream(FederatedStore.class, PATH_BASIC_ENTITY_SCHEMA_JSON))
                        .build());

        store.initialise(FEDERATED_STORE_ID, federatedProperties, mockLibrary);

        assertEquals(1, store.getGraphs().size());

        Mockito.verify(mockLibrary).getSchema(SCHEMA_ID_1);
    }

    @Test
    public void shouldAddGraphWithPropertiesAndSchemaFromGraphLibrary() throws Exception {
        federatedProperties.set(GRAPH_IDS, MAP_ID_1);
        federatedProperties.set(KEY_MAP_ID1_PROPERTIES_ID, PROPS_ID_1);
        federatedProperties.set(KEY_MAP_ID1_SCHEMA_ID, SCHEMA_ID_1);
        final GraphLibrary mockLibrary = Mockito.mock(GraphLibrary.class);
        Mockito.when(mockLibrary.getProperties(PROPS_ID_1))
                .thenReturn(StoreProperties.loadStoreProperties(PATH_MAP_STORE_PROPERTIES));
        Mockito.when(mockLibrary.getSchema(SCHEMA_ID_1))
                .thenReturn(new Schema.Builder()
                        .json(StreamUtil.openStream(FederatedStore.class, PATH_BASIC_ENTITY_SCHEMA_JSON))
                        .build());

        store.initialise(FEDERATED_STORE_ID, federatedProperties, mockLibrary);

        assertEquals(1, store.getGraphs().size());

        Mockito.verify(mockLibrary).getSchema(SCHEMA_ID_1);
        Mockito.verify(mockLibrary).getProperties(PROPS_ID_1);
    }

    @Test
    public void shouldAddGraphWithPropertiesFromGraphLibraryOverridden() throws Exception {
        federatedProperties.set(GRAPH_IDS, MAP_ID_1);
        federatedProperties.set(KEY_MAP_ID1_PROPERTIES_ID, PROPS_ID_1);
        federatedProperties.set(KEY_MAP_ID1_PROPERTIES_FILE, PATH_MAP_STORE_PROPERTIES);
        federatedProperties.set(KEY_MAP_ID1_SCHEMA_FILE, PATH_BASIC_EDGE_SCHEMA_JSON);
        final GraphLibrary mockLibrary = Mockito.mock(GraphLibrary.class);
        final MapStoreProperties prop = new MapStoreProperties();
        final String unusualKey = "unusualKey";
        prop.set(unusualKey, "value");
        Mockito.when(mockLibrary.getProperties(PROPS_ID_1)).thenReturn(prop);

        store.initialise(FEDERATED_STORE_ID, federatedProperties, mockLibrary);

        assertEquals(1, store.getGraphs().size());
        assertTrue(store.getGraphs().iterator().next().getStoreProperties().getProperties().getProperty(unusualKey) != null);

        Mockito.verify(mockLibrary).getProperties(PROPS_ID_1);
    }

    @Test
    public void shouldAddGraphWithSchemaFromGraphLibraryOverridden() throws Exception {
        federatedProperties.set(GRAPH_IDS, MAP_ID_1);
        federatedProperties.set(KEY_MAP_ID1_PROPERTIES_FILE, PATH_MAP_STORE_PROPERTIES);
        federatedProperties.set(KEY_MAP_ID1_SCHEMA_FILE, PATH_BASIC_EDGE_SCHEMA_JSON);
        federatedProperties.set(KEY_MAP_ID1_SCHEMA_ID, SCHEMA_ID_1);
        final GraphLibrary mockLibrary = Mockito.mock(GraphLibrary.class);
        Mockito.when(mockLibrary.getSchema(SCHEMA_ID_1)).thenReturn(new Schema.Builder()
                .json(StreamUtil.openStream(this.getClass(), PATH_BASIC_ENTITY_SCHEMA_JSON))
                .build());

        store.initialise(FEDERATED_STORE_ID, federatedProperties, mockLibrary);

        assertEquals(1, store.getGraphs().size());
        assertTrue(store.getGraphs().iterator().next().getSchema().getEntityGroups().contains("BasicEntity"));

        Mockito.verify(mockLibrary).getSchema(SCHEMA_ID_1);
    }

    @Test
    public void shouldAddGraphWithPropertiesAndSchemaFromGraphLibraryOverridden() throws Exception {
        federatedProperties.set(GRAPH_IDS, MAP_ID_1);
        federatedProperties.set(KEY_MAP_ID1_PROPERTIES_ID, PROPS_ID_1);
        federatedProperties.set(KEY_MAP_ID1_PROPERTIES_FILE, PATH_MAP_STORE_PROPERTIES);
        federatedProperties.set(KEY_MAP_ID1_SCHEMA_FILE, PATH_BASIC_EDGE_SCHEMA_JSON);
        federatedProperties.set(KEY_MAP_ID1_SCHEMA_ID, SCHEMA_ID_1);
        final GraphLibrary mockLibrary = Mockito.mock(GraphLibrary.class);
        final MapStoreProperties prop = new MapStoreProperties();
        final String unusualKey = "unusualKey";
        prop.set(unusualKey, "value");
        Mockito.when(mockLibrary.getProperties(PROPS_ID_1)).thenReturn(prop);
        Mockito.when(mockLibrary.getSchema(SCHEMA_ID_1)).thenReturn(new Schema.Builder()
                .json(StreamUtil.openStream(this.getClass(), PATH_BASIC_ENTITY_SCHEMA_JSON))
                .build());

        store.initialise(FEDERATED_STORE_ID, federatedProperties, mockLibrary);

        assertEquals(1, store.getGraphs().size());
        assertTrue(store.getGraphs().iterator().next().getStoreProperties().getProperties().getProperty(unusualKey) != null);
        assertTrue(store.getGraphs().iterator().next().getSchema().getEntityGroups().contains("BasicEntity"));

        Mockito.verify(mockLibrary).getProperties(PROPS_ID_1);
        Mockito.verify(mockLibrary).getSchema(SCHEMA_ID_1);
    }

    @Test
    public void should() throws Exception {
        //Given
        federatedProperties.set(GRAPH_IDS, MAP_ID_1);
        federatedProperties.set(KEY_MAP_ID1_PROPERTIES_FILE, PATH_MAP_STORE_PROPERTIES);
        federatedProperties.set(KEY_MAP_ID1_SCHEMA_FILE, PATH_BASIC_EDGE_SCHEMA_JSON);
        final GraphLibrary mockLibrary = Mockito.mock(GraphLibrary.class);
        Mockito.when(mockLibrary.get(MAP_ID_1)).thenReturn(
                new Pair<>(
                        new Schema.Builder()
                                .json(StreamUtil.openStream(this.getClass(), PATH_BASIC_EDGE_SCHEMA_JSON))
                                .build(),
                        StoreProperties.loadStoreProperties(StreamUtil.openStream(this.getClass(), PATH_MAP_STORE_PROPERTIES))
                ));
        final MapStoreProperties prop = new MapStoreProperties();
        final String unusualKey = "unusualKey";
        prop.set(unusualKey, "value");
        Mockito.when(mockLibrary.getProperties(PROPS_ID_1)).thenReturn(prop);
        Mockito.when(mockLibrary.getSchema(SCHEMA_ID_1)).thenReturn(new Schema.Builder()
                .json(StreamUtil.openStream(this.getClass(), PATH_BASIC_ENTITY_SCHEMA_JSON))
                .build());

        store.initialise(FEDERATED_STORE_ID, federatedProperties, mockLibrary);

        //No exception to be thrown.
    }

    @Test
    public void shouldNotAllowOverridingOfKnownGraphInLibrary() throws Exception {
        //Given
        federatedProperties.set(GRAPH_IDS, MAP_ID_1);
        federatedProperties.set(KEY_MAP_ID1_PROPERTIES_FILE, PATH_MAP_STORE_PROPERTIES_ALT);
        federatedProperties.set(KEY_MAP_ID1_SCHEMA_FILE, PATH_BASIC_EDGE_SCHEMA_JSON);
        final Schema schema = new Builder()
                .json(StreamUtil.openStream(this.getClass(), PATH_BASIC_EDGE_SCHEMA_JSON))
                .build();

        final StoreProperties storeProperties = StoreProperties.loadStoreProperties(StreamUtil.openStream(this.getClass(), PATH_MAP_STORE_PROPERTIES));
        final GraphLibrary lib = new HashMapGraphLibrary();
        lib.add(MAP_ID_1, schema, storeProperties);

        try {
            store.initialise(FEDERATED_STORE_ID, federatedProperties, lib);
            fail("exception should have been thrown");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("User is attempting to override a known graph in library."));
        }
    }


}
