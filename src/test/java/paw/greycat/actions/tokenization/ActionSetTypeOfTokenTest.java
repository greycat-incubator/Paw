package paw.greycat.actions.tokenization;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import paw.greycat.actions.ActionTest;

import static org.junit.jupiter.api.Assertions.*;

class ActionSetTypeOfTokenTest extends ActionTest {
    @BeforeEach
    public void setUp() {
        initGraph();
    }

    @AfterEach
    public void tearDown() {
        removeGraph();
    }
}