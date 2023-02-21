package cn.labzen.plugin.api.bean;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class ValuesTest {

  @Test
  public void testValues() {
    Address address = new Address();
    address.setProvince("山东");
    address.setCity("青岛");
    address.setDistrict("崂山");
    address.setStreet("海尔路");

    Values values = new Values();
    values.set("Name", "张三");
    values.set("address", address);
    values.set("dead", true);
    values.set("dob", "2020-01-01");
    HashMap<String, Object> information = Maps.newHashMap();
    information.put("salary", 1800);
    information.put("job", "coder");
    values.set("information", information);

    Target target = values.whole(Target.class);
    Assertions.assertNull(target.getName());
    Assertions.assertNotNull(target.getAddress());
    Assertions.assertNull(target.getDob());
    Assertions.assertTrue(target.getDead());
    Address targetAddress = target.getAddress();
    Assertions.assertNotNull(targetAddress.getCity());
    Assertions.assertEquals("海尔路", targetAddress.getStreet());
  }

  @Test
  public void testTransmittableValues() {
    Address address = new Address();
    address.setProvince("山东");
    address.setCity("青岛");
    address.setDistrict("崂山");
    address.setStreet("海尔路");

    Source source = new Source();
    source.setName("张三");
    source.setAge(18);
    source.setAddress(address);
    source.setParentAndChildrenNames(Lists.newArrayList("张二", "张小三"));
    HashMap<String, Object> information = Maps.newHashMap();
    information.put("salary", 1800);
    information.put("job", "coder");
    source.setInformation(information);

    Values values = Values.transmit(source);
    Assertions.assertTrue(values.isPresent("name"));
    Assertions.assertFalse(values.isPresent("mistressName"));
    Assertions.assertNotNull(values.get("address"));

    Map<String, Object> wholeValuesAsMap = values.whole();
    Assertions.assertNotNull(wholeValuesAsMap);
    Assertions.assertTrue(wholeValuesAsMap.containsKey("name"));
    Assertions.assertFalse(wholeValuesAsMap.containsValue("mistressName"));
    Assertions.assertEquals(7, wholeValuesAsMap.size());

    Target target = values.whole(Target.class);
    Assertions.assertEquals("张三", target.getName());
    Assertions.assertNotNull(target.getAddress());
    Assertions.assertNull(target.getDob());
    Assertions.assertEquals("coder", target.getInformation().get("job"));
    Address targetAddress = target.getAddress();
    Assertions.assertNotNull(targetAddress.getCity());
    Assertions.assertEquals("海尔路", targetAddress.getStreet());
  }
}
