//usr/bin/env jshell --add-exports jdk.jconsole/sun.tools.jconsole "$0" -R -DjobName="$@"; exit $?

// 실행 방법 : ./stop.jsh [jobName]
// 예시 : ./stop.jsh slowJob

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import sun.tools.jconsole.LocalVirtualMachine;

int getPid(String keyword) {
    Map<Integer, LocalVirtualMachine> vms = LocalVirtualMachine.getAllVirtualMachines();
    return vms.entrySet().stream()
        .filter(entry -> entry.getValue().toString().contains(keyword))
        .findFirst()
        .map(Map.Entry::getKey)
        .orElseThrow(() -> new IllegalStateException("cannot find process by " + keyword));
}

JMXConnector connect(int pid) throws IOException {
    LocalVirtualMachine vm = LocalVirtualMachine.getLocalVirtualMachine(pid);
    vm.startManagementAgent();
    String connectorAddress = vm.connectorAddress();
    var jmxUrl = new JMXServiceURL(connectorAddress);
    return JMXConnectorFactory.connect(jmxUrl);
}

void run(int pid, String beanName, String operation, Object[] params, String[] signature) {
    try (JMXConnector connector = connect(pid)) {
        MBeanServerConnection connection = connector.getMBeanServerConnection();
        connection.invoke(new ObjectName(beanName), operation, params, signature);
    } catch (Exception ex) {
        throw new RuntimeException("fail to execute " + operation, ex);
    }
}

String jobName = System.getProperty("jobName");
int pid = getPid("CommandLineJobOperator");
System.out.println("PID : " + pid);

String beanName = "kr.co.wikibook.batch.support:type=JobService,name=jobService";
String operation = "stopExecutions";
Object[] params = new Object[] {jobName};
String[] signature = new String[] {"java.lang.String"};
run(pid, beanName, operation, params, signature);
System.out.println(operation + " operation executed");
/exit
