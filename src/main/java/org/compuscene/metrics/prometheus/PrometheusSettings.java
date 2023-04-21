/*
 * Copyright [2019] [Lukáš VLČEK]
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
 *
 */

package org.compuscene.metrics.prometheus;

import org.opensearch.action.support.IndicesOptions;
import org.opensearch.common.Strings;
import org.opensearch.common.settings.ClusterSettings;
import org.opensearch.common.settings.Setting;
import org.opensearch.common.settings.Settings;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Dynamically updatable Prometheus exporter settings.
 *
 * These settings are part of cluster state available via
 * <pre>{@code
 * curl <opensearch>/_cluster/settings?include_defaults=true&filter_path=defaults.prometheus
 * }</pre>
 */
public class PrometheusSettings {
    public enum INDEX_FILTER_OPTIONS {
        STRICT_EXPAND_OPEN,
        STRICT_EXPAND_OPEN_HIDDEN,
        STRICT_EXPAND_OPEN_FORBID_CLOSED,
        STRICT_EXPAND_OPEN_FORBID_CLOSED_IGNORE_THROTTLED,
        STRICT_EXPAND_OPEN_CLOSED,
        STRICT_EXPAND_OPEN_CLOSED_HIDDEN,
        STRICT_SINGLE_INDEX_NO_EXPAND_FORBID_CLOSED,
        LENIENT_EXPAND_OPEN,
        LENIENT_EXPAND_OPEN_HIDDEN,
        LENIENT_EXPAND_OPEN_CLOSED,
        LENIENT_EXPAND_OPEN_CLOSED_HIDDEN
    }

    static String PROMETHEUS_CLUSTER_SETTINGS_KEY = "prometheus.cluster.settings";
    static String PROMETHEUS_INDICES_KEY = "prometheus.indices";
    static String PROMETHEUS_NODES_FILTER_KEY = "prometheus.nodes.filter";
    static String PROMETHEUS_SELECTED_INDICES_KEY = "prometheus.indices_filter.selected_indices";
    static String PROMETHEUS_SELECTED_OPTION_KEY = "prometheus.indices_filter.selected_option";
    static String PROMETHEUS_OPTIONS_DESCRIPTION_KEY = "prometheus.indices_filter.options_description";

    /**
     * This setting is used configure weather to expose cluster settings metrics or not. The default value is true.
     * Can be configured in opensearch.yml file or update dynamically under key {@link #PROMETHEUS_CLUSTER_SETTINGS_KEY}.
     */
    public static final Setting<Boolean> PROMETHEUS_CLUSTER_SETTINGS =
            Setting.boolSetting(PROMETHEUS_CLUSTER_SETTINGS_KEY, true,
                    Setting.Property.Dynamic, Setting.Property.NodeScope);

    /**
     * This setting is used configure weather to expose low level index metrics or not. The default value is true.
     * Can be configured in opensearch.yml file or update dynamically under key {@link #PROMETHEUS_INDICES_KEY}.
     */
    public static final Setting<Boolean> PROMETHEUS_INDICES =
            Setting.boolSetting(PROMETHEUS_INDICES_KEY, true,
                    Setting.Property.Dynamic, Setting.Property.NodeScope);

    /**
     * This setting is used configure which cluster nodes to gather metrics from. The default value is _local.
     * Can be configured in opensearch.yml file or update dynamically under key {@link #PROMETHEUS_NODES_FILTER_KEY}.
     */
    public static final Setting<String> PROMETHEUS_NODES_FILTER =
            Setting.simpleString(PROMETHEUS_NODES_FILTER_KEY, "_local",
                    Setting.Property.Dynamic, Setting.Property.NodeScope);

    /**
     * This setting is used configure to filter indices statistics from selected indices. The default value is "".
     * Can be configured in opensearch.yml file or update dynamically under key {@link #PROMETHEUS_SELECTED_INDICES_KEY}.
     */
    public static final Setting<String> PROMETHEUS_SELECTED_INDICES =
            Setting.simpleString(PROMETHEUS_SELECTED_INDICES_KEY, "",
                    Setting.Property.Dynamic, Setting.Property.NodeScope);

    /**
     * This setting is used configure which option for filtering indices statistics.
     * The default value is STRICT_EXPAND_OPEN_FORBID_CLOSED.
     * Can be configured in opensearch.yml file or update dynamically under key {@link #PROMETHEUS_SELECTED_OPTION_KEY}.
     */
    public static final Setting<INDEX_FILTER_OPTIONS> PROMETHEUS_SELECTED_OPTION =
            new Setting<>(PROMETHEUS_SELECTED_OPTION_KEY,
                    String.valueOf(INDEX_FILTER_OPTIONS.STRICT_EXPAND_OPEN_FORBID_CLOSED),
                    INDEX_FILTER_OPTIONS::valueOf, Setting.Property.Dynamic, Setting.Property.NodeScope);

    /**
     * This setting is used to display options description
     */
    public static final Setting<List<String>> PROMETHEUS_OPTIONS_DESCRIPTION =
            Setting.listSetting(PROMETHEUS_OPTIONS_DESCRIPTION_KEY,
                    Arrays.asList(
                            "STRICT_EXPAND_OPEN: indices options that requires every specified index to exist, expands wildcards only to open indices and allows that no indices are resolved from wildcard expressions (not returning an error).",
                            "STRICT_EXPAND_OPEN_HIDDEN: indices options that requires every specified index to exist, expands wildcards only to open indices, includes hidden indices, and allows that no indices are resolved from wildcard expressions (not returning an error).",
                            "STRICT_EXPAND_OPEN_FORBID_CLOSED: indices options that requires every specified index to exist, expands wildcards only to open indices, allows that no indices are resolved from wildcard expressions (not returning an error) and forbids the use of closed indices by throwing an error.",
                            "STRICT_EXPAND_OPEN_FORBID_CLOSED_IGNORE_THROTTLED: indices options that requires every specified index to exist, expands wildcards only to open indices, allows that no indices are resolved from wildcard expressions (not returning an error), forbids the use of closed indices by throwing an error and ignores indices that are throttled.",
                            "STRICT_EXPAND_OPEN_CLOSED: indices option that requires every specified index to exist, expands wildcards to both open and closed indices and allows that no indices are resolved from wildcard expressions (not returning an error).",
                            "STRICT_EXPAND_OPEN_CLOSED_HIDDEN: indices option that requires every specified index to exist, expands wildcards to both open and closed indices, includes hidden indices, and allows that no indices are resolved from wildcard expressions (not returning an error).",
                            "STRICT_SINGLE_INDEX_NO_EXPAND_FORBID_CLOSED: indices option that requires each specified index or alias to exist, doesn't expand wildcards and throws error if any of the aliases resolves to multiple indices.",
                            "LENIENT_EXPAND_OPEN: indices options that ignores unavailable indices, expands wildcards only to open indices and allows that no indices are resolved from wildcard expressions (not returning an error).",
                            "LENIENT_EXPAND_OPEN_HIDDEN: indices options that ignores unavailable indices, expands wildcards to open and hidden indices, and allows that no indices are resolved from wildcard expressions (not returning an error).",
                            "LENIENT_EXPAND_OPEN_CLOSED: indices options that ignores unavailable indices, expands wildcards to both open and closed indices and allows that no indices are resolved from wildcard expressions (not returning an error).",
                            "LENIENT_EXPAND_OPEN_CLOSED_HIDDEN: indices options that ignores unavailable indices, expands wildcards to all open and closed indices and allows that no indices are resolved from wildcard expressions (not returning an error)."
                    ),
                    Function.identity(), Setting.Property.NodeScope);

    private volatile boolean clusterSettings;
    private volatile boolean indices;
    private volatile String nodesFilter;
    private volatile String selectedIndices;
    private volatile INDEX_FILTER_OPTIONS selectedOption;
    private volatile List<String> optionsDescription;

    /**
     * A constructor.
     * @param settings Settings
     * @param clusterSettings Cluster settings
     */
    public PrometheusSettings(Settings settings, ClusterSettings clusterSettings) {
        setPrometheusClusterSettings(PROMETHEUS_CLUSTER_SETTINGS.get(settings));
        setPrometheusIndices(PROMETHEUS_INDICES.get(settings));
        setPrometheusNodesFilter(PROMETHEUS_NODES_FILTER.get(settings));
        setPrometheusSelectedIndices(PROMETHEUS_SELECTED_INDICES.get(settings));
        setPrometheusSelectedOption(PROMETHEUS_SELECTED_OPTION.get(settings));
        setPrometheusOptionsDescription(PROMETHEUS_OPTIONS_DESCRIPTION.get(settings));
        clusterSettings.addSettingsUpdateConsumer(PROMETHEUS_CLUSTER_SETTINGS, this::setPrometheusClusterSettings);
        clusterSettings.addSettingsUpdateConsumer(PROMETHEUS_INDICES, this::setPrometheusIndices);
        clusterSettings.addSettingsUpdateConsumer(PROMETHEUS_NODES_FILTER, this::setPrometheusNodesFilter);
        clusterSettings.addSettingsUpdateConsumer(PROMETHEUS_SELECTED_INDICES, this::setPrometheusSelectedIndices);
        clusterSettings.addSettingsUpdateConsumer(PROMETHEUS_SELECTED_OPTION, this::setPrometheusSelectedOption);
    }

    private void setPrometheusClusterSettings(boolean flag) {
        this.clusterSettings = flag;
    }

    private void setPrometheusIndices(boolean flag) {
        this.indices = flag;
    }

    private void setPrometheusNodesFilter(String filter) { this.nodesFilter = filter; }

    private void setPrometheusSelectedIndices(String selectedIndices) {
        this.selectedIndices = selectedIndices;
    }

    private void setPrometheusSelectedOption(INDEX_FILTER_OPTIONS selectedOption) {
        this.selectedOption = selectedOption;
    }

    private void setPrometheusOptionsDescription(List<String> optionsDescription) {
        this.optionsDescription = optionsDescription;
    }

    /**
     * Get value of settings key {@link #PROMETHEUS_CLUSTER_SETTINGS_KEY}.
     * @return boolean value of the key
     */
    public boolean getPrometheusClusterSettings() {
        return this.clusterSettings;
    }

    /**
     * Get value of settings key {@link #PROMETHEUS_INDICES_KEY}.
     * @return boolean value of the key
     */
    public boolean getPrometheusIndices() {
        return this.indices;
    }

    /**
     * Get value of settings key {@link #PROMETHEUS_NODES_FILTER_KEY}.
     * @return boolean value of the key
     */
    public String getNodesFilter() { return this.nodesFilter; }

    /**
     * Get value of settings key {@link #PROMETHEUS_SELECTED_INDICES_KEY}.
     * @return string value of the key
     */
    public String[] getPrometheusSelectedIndices() {
        return Strings.splitStringByCommaToArray(this.selectedIndices);
    }

    /**
     * Get IndicesOptions of settings key {@link #PROMETHEUS_SELECTED_OPTION_KEY}.
     * @return IndicesOptions of the key
     */
    public IndicesOptions getIndicesOptions() {
        IndicesOptions indicesOptions = null;
        switch (this.selectedOption) {
            case STRICT_EXPAND_OPEN:
                indicesOptions = IndicesOptions.strictExpandOpen();
                break;
            case STRICT_EXPAND_OPEN_HIDDEN:
                indicesOptions = IndicesOptions.strictExpandOpenHidden();
                break;
            case STRICT_EXPAND_OPEN_FORBID_CLOSED:
                indicesOptions = IndicesOptions.strictExpandOpenAndForbidClosed();
                break;
            case STRICT_EXPAND_OPEN_FORBID_CLOSED_IGNORE_THROTTLED:
                indicesOptions = IndicesOptions.strictExpandOpenAndForbidClosedIgnoreThrottled();
                break;
            case STRICT_EXPAND_OPEN_CLOSED:
                indicesOptions = IndicesOptions.strictExpand();
                break;
            case STRICT_EXPAND_OPEN_CLOSED_HIDDEN:
                indicesOptions = IndicesOptions.strictExpandHidden();
                break;
            case STRICT_SINGLE_INDEX_NO_EXPAND_FORBID_CLOSED:
                indicesOptions = IndicesOptions.strictSingleIndexNoExpandForbidClosed();
                break;
            case LENIENT_EXPAND_OPEN:
                indicesOptions = IndicesOptions.lenientExpandOpen();
                break;
            case LENIENT_EXPAND_OPEN_HIDDEN:
                indicesOptions = IndicesOptions.lenientExpandOpenHidden();
                break;
            case LENIENT_EXPAND_OPEN_CLOSED:
                indicesOptions = IndicesOptions.lenientExpand();
                break;
            case LENIENT_EXPAND_OPEN_CLOSED_HIDDEN:
                indicesOptions = IndicesOptions.lenientExpandHidden();
                break;
        }
        return indicesOptions;
    }
}
