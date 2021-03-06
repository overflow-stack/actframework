package act.metric;

import act.cli.tree.FilteredTreeNode;
import act.cli.tree.TreeNode;
import act.cli.tree.TreeNodeFilter;
import org.osgl.$;
import org.osgl.util.C;
import org.osgl.util.S;

import java.util.List;
import java.util.Map;

public class MetricInfoTree {

    private C.Map<String, MetricInfo> infoMap = C.newMap();
    private MetricInfoNode root;
    private C.Map<String, MetricInfoNode> nodeMap = C.newMap();
    private TreeNodeFilter filter = null;


    public MetricInfoTree(List<MetricInfo> infoList, $.Predicate<String> filter) {
        for (MetricInfo info : infoList) {
            infoMap.put(info.getName(), info);
        }
        if (filter != $.F.TRUE) {
            this.filter = TreeNodeFilter.Common.pathMatches(filter);
        }
        buildTree();
    }

    public TreeNode root(NodeDecorator decorator) {
        return null == filter ? decorator.decorate(root) : new FilteredTreeNode(decorator.decorate(root), filter);
    }

    MetricInfoNode getNode(MetricInfo info) {
        MetricInfoNode node = nodeMap.get(info.getName());
        if (null == node) {
            node = new MetricInfoNode(info);
            nodeMap.put(info.getName(), node);
        }
        return node;
    }

    MetricInfo getMetricInfo(String name) {
        return infoMap.get(name);
    }

    void buildTree() {
        for (Map.Entry<String, MetricInfo> entry: infoMap.entrySet()) {
            String path = entry.getKey();
            MetricInfo info = entry.getValue();
            MetricInfoNode node = getNode(info);
            if (node.isRoot) {
                root = node;
            }
        }
    }

    class MetricInfoNode implements TreeNode {

        MetricInfo info;
        C.List<TreeNode> children = C.newList();
        private boolean isRoot;

        MetricInfoNode(MetricInfo info) {
            this.info = $.notNull(info);
            this.isRoot = !addToParent();
        }

        @Override
        public String id() {
            return S.str(info.getName()).afterLast(Metric.PATH_SEPARATOR).toString();
        }

        @Override
        public String label() {
            return info.getName();
        }

        @Override
        public C.List<TreeNode> children() {
            return children;
        }

        public String getParentPath() {
            String path = info.getName();
            if (path.contains(Metric.PATH_SEPARATOR)) {
                return S.beforeLast(path, Metric.PATH_SEPARATOR);
            }
            return "";
        }

        boolean addToParent() {
            String parentPath = getParentPath();
            if (S.blank(parentPath)) {
                return false;
            }
            MetricInfo parent = getMetricInfo(parentPath);
            if (null == parent) {
                return false;
            }
            MetricInfoNode parentNode = getNode(parent);
            parentNode.children.add(this);
            return true;
        }
    }

    public static class NodeDecorator {
        $.Function<MetricInfo, String> labelGetter;

        NodeDecorator($.Function<MetricInfo, String> labelGetter) {
            this.labelGetter = $.notNull(labelGetter);
        }

        TreeNode decorate(final MetricInfoNode node) {
            return new TreeNode() {

                @Override
                public String id() {
                    return node.id();
                }

                @Override
                public String label() {
                    return labelGetter.apply(node.info);
                }

                @Override
                public List<TreeNode> children() {
                    return node.children().map(new $.Transformer<TreeNode, TreeNode>() {
                        @Override
                        public TreeNode transform(TreeNode treeNode) {
                            return decorate((MetricInfoNode) treeNode);
                        }
                    });
                }

            };
        }

    }

    static final NodeDecorator COUNTER = new NodeDecorator(new $.Transformer<MetricInfo, String>() {
        @Override
        public String transform(MetricInfo metricInfo) {
            return S.fmt("%s: %s", metricInfo.getName(), metricInfo.getCountAsStr());
        }
    });

    static final NodeDecorator TIMER = new NodeDecorator(new $.Transformer<MetricInfo, String>() {
        @Override
        public String transform(MetricInfo metricInfo) {
            return S.fmt("%s: %s / %s = %s", metricInfo.getName(), metricInfo.getAccumulated(), metricInfo.getCountAsStr(), metricInfo.getAvg());
        }
    });

}
