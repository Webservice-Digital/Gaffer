/*
 * Copyright 2016 Crown Copyright
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
package uk.gov.gchq.gaffer.example.gettingstarted.analytic;

import com.yahoo.sketches.sampling.ReservoirItemsSketch;
import com.yahoo.sketches.sampling.ReservoirItemsUnion;
import uk.gov.gchq.gaffer.data.element.Edge;
import uk.gov.gchq.gaffer.data.element.Entity;
import uk.gov.gchq.gaffer.data.elementdefinition.view.View;
import uk.gov.gchq.gaffer.example.gettingstarted.generator.DataGenerator12;
import uk.gov.gchq.gaffer.graph.Graph;
import uk.gov.gchq.gaffer.operation.OperationChain;
import uk.gov.gchq.gaffer.operation.OperationException;
import uk.gov.gchq.gaffer.operation.data.EdgeSeed;
import uk.gov.gchq.gaffer.operation.data.EntitySeed;
import uk.gov.gchq.gaffer.operation.impl.add.AddElements;
import uk.gov.gchq.gaffer.operation.impl.generate.GenerateElements;
import uk.gov.gchq.gaffer.operation.impl.get.GetAllEdges;
import uk.gov.gchq.gaffer.operation.impl.get.GetEdges;
import uk.gov.gchq.gaffer.operation.impl.get.GetEntities;
import uk.gov.gchq.gaffer.user.User;

import java.util.Collections;
import java.util.Set;

public class LoadAndQuery12 extends LoadAndQuery {
    public LoadAndQuery12() {
        super("Using ReservoirItemsUnion to store a random sample of strings seen on an Element");
    }

    public static void main(final String[] args) throws OperationException {
        new LoadAndQuery12().run();
    }

    public Iterable<Entity> run() throws OperationException {
        // [user] Create a user
        // ---------------------------------------------------------
        final User user = new User("user01");
        // ---------------------------------------------------------


        // [graph] create a graph using our schema and store properties
        // ---------------------------------------------------------
        final Graph graph = new Graph.Builder()
                .addSchemas(getSchemas())
                .storeProperties(getStoreProperties())
                .build();
        // ---------------------------------------------------------


        // [add] add the edges to the graph
        // ---------------------------------------------------------
        final Set<String> dummyData = Collections.singleton("");
        final OperationChain addOpChain = new OperationChain.Builder()
                .first(new GenerateElements.Builder<String>()
                        .generator(new DataGenerator12())
                        .objects(dummyData)
                        .build())
                .then(new AddElements())
                .build();

        graph.execute(addOpChain, user);
        // ---------------------------------------------------------


        // [get red edge] Get the red edge
        // ---------------------------------------------------------
        final GetAllEdges getAllEdges = new GetAllEdges.Builder()
                .view(new View.Builder()
                        .edge("red")
                        .build())
                .build();
        final Iterable<Edge> allEdges = graph.execute(getAllEdges, user);
        // ---------------------------------------------------------
        log("\nThe red edge A-B:");
        for (final Edge edge : allEdges) {
            log("GET_A-B_EDGE_RESULT", edge.toString());
        }


        // [get strings sample from the red edge] Get the edge A-B and print out the sample of strings
        // ---------------------------------------------------------
        final GetEdges<EdgeSeed> query = new GetEdges.Builder<EdgeSeed>()
                .addSeed(new EdgeSeed("A", "B", false))
                .build();
        final Iterable<Edge> edges = graph.execute(query, user);
        final Edge edge = edges.iterator().next();
        final ReservoirItemsSketch<String> stringsSketch = ((ReservoirItemsUnion<String>) edge.getProperty("stringsSample")).getResult();
        final String[] samples = stringsSketch.getSamples();
        String sampleResult = "10 samples: ";
        for (int i = 0; i < 10 && i < samples.length; i++) {
            if (i > 0) {
                sampleResult += ", ";
            }
            sampleResult += samples[i];
        }
        // ---------------------------------------------------------
        log("\nEdge A-B with a sample of the strings");
        log("GET_SAMPLE_FOR_RED_EDGE", sampleResult);


        // [get sample from the blue entity] Get the entity Y and print a sample of the neighbours
        // ---------------------------------------------------------
        final GetEntities<EntitySeed> query2 = new GetEntities.Builder<EntitySeed>()
                .addSeed(new EntitySeed("X"))
                .build();
        final Iterable<Entity> entities = graph.execute(query2, user);
        final Entity entity = entities.iterator().next();
        final ReservoirItemsSketch<String> neighboursSketch = ((ReservoirItemsUnion<String>) entity.getProperty("neighboursSample")).getResult();
        final String[] neighboursSample = neighboursSketch.getSamples();
        String neighbourSampleResult = "10 samples: ";
        for (int i = 0; i < 10 && i < neighboursSample.length; i++) {
            neighbourSampleResult += neighboursSample[i] + ", ";
        }
        // ---------------------------------------------------------
        log("\nEntity for vertex X with a sample of its neighbouring vertices");
        log("GET_SAMPLES_FOR_X_RESULT", neighbourSampleResult);
        return null;
    }
}
