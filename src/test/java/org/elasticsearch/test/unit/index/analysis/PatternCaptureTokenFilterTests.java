/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.test.unit.index.analysis;

import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNameModule;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.testng.annotations.Test;

import java.io.StringReader;

import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import static org.elasticsearch.test.unit.index.analysis.AnalysisTestsHelper.assertSimpleTSOutput;

public class PatternCaptureTokenFilterTests {

    @Test
    public void testPatternCaptureTokenFilter() throws Exception {
        Index index = new Index("test");
        Settings settings = settingsBuilder().loadFromClasspath("org/elasticsearch/test/unit/index/analysis/pattern_capture.json").build();
        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(settings), new EnvironmentModule(new Environment(settings)), new IndicesAnalysisModule()).createInjector();
        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, settings),
                new IndexNameModule(index),
                new AnalysisModule(settings, parentInjector.getInstance(IndicesAnalysisService.class)))
                .createChildInjector(parentInjector);

        AnalysisService analysisService = injector.getInstance(AnalysisService.class);

        NamedAnalyzer analyzer1 = analysisService.analyzer("single");

        assertSimpleTSOutput(analyzer1.tokenStream("test", new StringReader("foobarbaz")), new String[]{"foobarbaz","foobar","foo"});

        NamedAnalyzer analyzer2 = analysisService.analyzer("multi");

        assertSimpleTSOutput(analyzer2.tokenStream("test", new StringReader("abc123def")), new String[]{"abc123def","abc","123","def"});

        NamedAnalyzer analyzer3 = analysisService.analyzer("preserve");

        assertSimpleTSOutput(analyzer3.tokenStream("test", new StringReader("foobarbaz")), new String[]{"foobar","foo"});

    }

}
