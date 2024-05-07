package nintendods.ds_project.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ANodeTest {

    @Test
    public void testGetName() {
        String name = "TestNode";
        ANode node = new ANode(name);
        assertEquals(name, node.getName());
    }

    @Test
    public void testSetName() {
        String name = "TestNode";
        ANode node = new ANode("");
        node.setName(name);
        assertEquals(name, node.getName());
    }

    @Test
    public void testToString() {
        String name = "TestNode";
        ANode node = new ANode(name);
        assertEquals("ANode{ name='" + name + "'}", node.toString());
    }
}
