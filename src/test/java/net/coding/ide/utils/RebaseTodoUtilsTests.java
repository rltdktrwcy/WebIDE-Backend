package net.coding.ide.utils;

import net.coding.ide.model.RebaseResponse;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.RebaseTodoLine;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RebaseTodoUtilsTests {

    @Test
    public void testLoadFromEmptyList() {
        List<RebaseTodoLine> lines = new ArrayList<>();
        List<RebaseResponse.RebaseTodoLine> result = RebaseTodoUtils.loadFrom(lines);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testLoadFromValidList() {
        List<RebaseTodoLine> lines = new ArrayList<>();
        lines.add(new RebaseTodoLine(RebaseTodoLine.Action.PICK,
                AbbreviatedObjectId.fromString("abc1234"), "First commit"));
        lines.add(new RebaseTodoLine(RebaseTodoLine.Action.EDIT,
                AbbreviatedObjectId.fromString("def5678"), "Second commit"));

        List<RebaseResponse.RebaseTodoLine> result = RebaseTodoUtils.loadFrom(lines);

        assertEquals(2, result.size());
        assertEquals("PICK", result.get(0).getAction().name());
        assertEquals("abc1234", result.get(0).getCommit());
        assertEquals("First commit", result.get(0).getShortMessage());
        assertEquals("EDIT", result.get(1).getAction().name());
        assertEquals("def5678", result.get(1).getCommit());
        assertEquals("Second commit", result.get(1).getShortMessage());
    }

    @Test
    public void testParseLinesEmptyList() {
        List<RebaseResponse.RebaseTodoLine> lines = new ArrayList<>();
        List<RebaseTodoLine> result = RebaseTodoUtils.parseLines(lines);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testParseLinesValidList() {
        List<RebaseResponse.RebaseTodoLine> lines = new ArrayList<>();
        lines.add(new RebaseResponse.RebaseTodoLine("PICK", "abc1234", "First commit"));
        lines.add(new RebaseResponse.RebaseTodoLine("EDIT", "def5678", "Second commit"));

        List<RebaseTodoLine> result = RebaseTodoUtils.parseLines(lines);

        assertEquals(2, result.size());
        assertEquals(RebaseTodoLine.Action.PICK, result.get(0).getAction());
        assertEquals("abc1234", result.get(0).getCommit().name());
        assertEquals("First commit", result.get(0).getShortMessage());
        assertEquals(RebaseTodoLine.Action.EDIT, result.get(1).getAction());
        assertEquals("def5678", result.get(1).getCommit().name());
        assertEquals("Second commit", result.get(1).getShortMessage());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseLinesInvalidAction() {
        List<RebaseResponse.RebaseTodoLine> lines = new ArrayList<>();
        lines.add(new RebaseResponse.RebaseTodoLine("INVALID", "abc1234", "Test commit"));
        RebaseTodoUtils.parseLines(lines);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseLinesInvalidCommitId() {
        List<RebaseResponse.RebaseTodoLine> lines = new ArrayList<>();
        lines.add(new RebaseResponse.RebaseTodoLine("PICK", "invalid", "Test commit"));
        RebaseTodoUtils.parseLines(lines);
    }
}
