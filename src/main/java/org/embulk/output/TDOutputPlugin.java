package org.embulk.output;

import java.util.List;
import org.embulk.config.CommitReport;
import org.embulk.config.Config;
import org.embulk.config.ConfigDefault;
import org.embulk.config.ConfigDiff;
import org.embulk.config.ConfigSource;
import org.embulk.config.Task;
import org.embulk.config.TaskSource;
import org.embulk.spi.Exec;
import org.embulk.spi.OutputPlugin;
import org.embulk.spi.PageOutput;
import org.embulk.spi.Schema;
import org.embulk.spi.TransactionalPageOutput;

public class TDOutputPlugin
        implements OutputPlugin
{
    public interface PluginTask
            extends Task
    {
        @Config("property1")
        public String getProperty1();

        @Config("property2")
        @ConfigDefault("0")
        public int getProperty2();
    }

    public ConfigDiff transaction(ConfigSource config,
            Schema schema, int processorCount,
            OutputPlugin.Control control)
    {
        PluginTask task = config.loadConfig(PluginTask.class);

        // retryable (idempotent) output:
        // return resume(task.dump(), schema, processorCount, control);

        // non-retryable (non-idempotent) output:
        control.run(task.dump());
        return Exec.newConfigDiff();
    }

    public ConfigDiff resume(TaskSource taskSource,
            Schema schema, int processorCount,
            OutputPlugin.Control control)
    {
        throw new UnsupportedOperationException("td output plugin does not support resuming");
    }

    public void cleanup(TaskSource taskSource,
            Schema schema, int processorCount,
            List<CommitReport> successCommitReports)
    {
    }

    public TransactionalPageOutput open(TaskSource taskSource, Schema schema, int processorIndex)
    {
        PluginTask task = taskSource.loadTask(PluginTask.class);

        // TODO
        throw new UnsupportedOperationException("The 'open' method needs to be implemented");
    }
}
