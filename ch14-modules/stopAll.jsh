//usr/bin/env jshell --add-exports jdk.jconsole/sun.tools.jconsole "$0" "$@"; exit $?

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

void run(int pid, String beanName, String operation) {
    try (JMXConnector connector = connect(pid)) {
        MBeanServerConnection connection = connector.getMBeanServerConnection();
        connection.invoke(new ObjectName(beanName), operation, new Object[0], new String[0]);
    } catch (Exception ex) {
        throw new RuntimeException("fail to execute " + operation, ex);
    }
}

int pid = getPid("CommandLineJobOperator"); // ps -ef | grep 으로 해당 프로세스를 잡을 수 있는 키워드를 넣는다.
System.out.println("PID : " + pid);

String beanName = "kr.co.wikibook.batch.support:type=JobService,name=jobService";
String operation = "stopAllExecutions";
run(pid, beanName, operation);
System.out.println(operation + " operation executed");
/exit
