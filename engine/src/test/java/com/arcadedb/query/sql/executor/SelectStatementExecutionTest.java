package com.arcadedb.query.sql.executor;

import com.arcadedb.TestHelper;
import com.arcadedb.database.Document;
import com.arcadedb.database.MutableDocument;
import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.schema.DocumentType;
import com.arcadedb.schema.Property;
import com.arcadedb.schema.Schema;
import com.arcadedb.schema.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

public class SelectStatementExecutionTest extends TestHelper {

    @Test
    public void testSelectNoTarget() {
        ResultSet result = database.query("sql", "select 1 as one, 2 as two, 2+3");
        Assertions.assertTrue(result.hasNext());
        Result item = result.next();
        Assertions.assertNotNull(item);
        Assertions.assertEquals(1, item.<Object>getProperty("one"));
        Assertions.assertEquals(2, item.<Object>getProperty("two"));
        Assertions.assertEquals(5, item.<Object>getProperty("2 + 3"));
        printExecutionPlan(result);

        result.close();
    }


    @Test
    public void testGroupByCount() {

        database.getSchema().createDocumentType("InputTx");

        database.begin();
        for (int i = 0; i < 100; i++) {
            final String hash = UUID.randomUUID().toString();
            database.command("sql", "insert into InputTx set address = '" + hash + "'");

            // CREATE RANDOM NUMBER OF COPIES final int random = new Random().nextInt(10);
            final int random = new Random().nextInt(10);
            for (int j = 0; j < random; j++) {
                database.command("sql", "insert into InputTx set address = '" + hash + "'");
            }
        }

        database.commit();
        final ResultSet result =
                database.query("sql",
                        "select address, count(*) as occurrencies from InputTx where address is not null group by address limit 10");
        while (result.hasNext()) {
            final Result row = result.next();
            Assertions.assertNotNull(row.getProperty("address")); // <== FALSE!
            Assertions.assertNotNull(row.getProperty("occurrencies"));
        }
        result.close();
    }

    @Test
    public void testSelectNoTargetSkip() {
        ResultSet result = database.query("sql", "select 1 as one, 2 as two, 2+3 skip 1");
        Assertions.assertFalse(result.hasNext());
        printExecutionPlan(result);

        result.close();
    }

    @Test
    public void testSelectNoTargetSkipZero() {
        ResultSet result = database.query("sql", "select 1 as one, 2 as two, 2+3 skip 0");
        Assertions.assertTrue(result.hasNext());
        Result item = result.next();
        Assertions.assertNotNull(item);
        Assertions.assertEquals(1, item.<Object>getProperty("one"));
        Assertions.assertEquals(2, item.<Object>getProperty("two"));
        Assertions.assertEquals(5, item.<Object>getProperty("2 + 3"));
        printExecutionPlan(result);

        result.close();
    }

    @Test
    public void testSelectNoTargetLimit0() {
        ResultSet result = database.query("sql", "select 1 as one, 2 as two, 2+3 limit 0");
        Assertions.assertFalse(result.hasNext());
        printExecutionPlan(result);

        result.close();
    }

    @Test
    public void testSelectNoTargetLimit1() {
        ResultSet result = database.query("sql", "select 1 as one, 2 as two, 2+3 limit 1");
        Assertions.assertTrue(result.hasNext());
        Result item = result.next();
        Assertions.assertNotNull(item);
        Assertions.assertEquals(1, item.<Object>getProperty("one"));
        Assertions.assertEquals(2, item.<Object>getProperty("two"));
        Assertions.assertEquals(5, item.<Object>getProperty("2 + 3"));
        printExecutionPlan(result);

        result.close();
    }

    @Test
    public void testSelectNoTargetLimitx() {
        ResultSet result = database.query("sql", "select 1 as one, 2 as two, 2+3 skip 0 limit 0");
        printExecutionPlan(result);
        result.close();
    }

    @Test
    public void testSelectFullScan1() {
        String className = "TestSelectFullScan1";
        database.getSchema().createDocumentType(className);
        database.begin();
        for (int i = 0; i < 100000; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("name", "name" + i);
            doc.set("surname", "surname" + i);
            doc.save();
        }
        database.commit();
        ResultSet result = database.query("sql", "select from " + className);
        for (int i = 0; i < 100000; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Assertions.assertNotNull(item);
            Assertions.assertTrue(("" + item.getProperty("name")).startsWith("name"));
        }
        Assertions.assertFalse(result.hasNext());
        printExecutionPlan(result);
        result.close();
    }

    @Test
    public void testSelectFullScanOrderByRidAsc() {
        String className = "testSelectFullScanOrderByRidAsc";
        database.getSchema().createDocumentType(className);
        database.begin();
        for (int i = 0; i < 100000; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("name", "name" + i);
            doc.set("surname", "surname" + i);
            doc.save();
        }
        database.commit();
        ResultSet result = database.query("sql", "select from " + className + " ORDER BY @rid ASC");
        printExecutionPlan(result);
        Document lastItem = null;
        for (int i = 0; i < 100000; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Assertions.assertNotNull(item);
            Assertions.assertTrue(("" + item.getProperty("name")).startsWith("name"));
            if (lastItem != null) {
                Assertions.assertTrue(
                        lastItem.getIdentity().compareTo(item.getElement().get().getIdentity()) < 0);
            }
            lastItem = item.getElement().get();
        }
        Assertions.assertFalse(result.hasNext());

        result.close();
    }

    @Test
    public void testSelectFullScanOrderByRidDesc() {
        String className = "testSelectFullScanOrderByRidDesc";
        database.getSchema().createDocumentType(className);
        database.begin();
        for (int i = 0; i < 100000; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("name", "name" + i);
            doc.set("surname", "surname" + i);
            doc.save();
        }
        database.commit();
        ResultSet result = database.query("sql", "select from " + className + " ORDER BY @rid DESC");
        printExecutionPlan(result);
        Document lastItem = null;
        for (int i = 0; i < 100000; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Assertions.assertNotNull(item);
            Assertions.assertTrue(("" + item.getProperty("name")).startsWith("name"));
            if (lastItem != null) {
                Assertions.assertTrue(
                        lastItem.getIdentity().compareTo(item.getElement().get().getIdentity()) > 0);
            }
            lastItem = item.getElement().get();
        }
        Assertions.assertFalse(result.hasNext());

        result.close();
    }

    @Test
    public void testSelectFullScanLimit1() {
        String className = "testSelectFullScanLimit1";
        database.getSchema().createDocumentType(className);

        database.begin();
        for (int i = 0; i < 300; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("name", "name" + i);
            doc.set("surname", "surname" + i);
            doc.save();
        }
        database.commit();
        ResultSet result = database.query("sql", "select from " + className + " limit 10");
        printExecutionPlan(result);

        for (int i = 0; i < 10; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Assertions.assertNotNull(item);
            Assertions.assertTrue(("" + item.getProperty("name")).startsWith("name"));
        }
        Assertions.assertFalse(result.hasNext());
        result.close();
    }

    @Test
    public void testSelectFullScanSkipLimit1() {
        String className = "testSelectFullScanSkipLimit1";
        database.getSchema().createDocumentType(className);

        database.begin();
        for (int i = 0; i < 300; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("name", "name" + i);
            doc.set("surname", "surname" + i);
            doc.save();
        }
        database.commit();
        ResultSet result = database.query("sql", "select from " + className + " skip 100 limit 10");
        printExecutionPlan(result);

        for (int i = 0; i < 10; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Assertions.assertNotNull(item);
            Assertions.assertTrue(("" + item.getProperty("name")).startsWith("name"));
        }
        Assertions.assertFalse(result.hasNext());
        result.close();
    }

    @Test
    public void testSelectOrderByDesc() {
        String className = "testSelectOrderByDesc";
        database.getSchema().createDocumentType(className);

        database.begin();
        for (int i = 0; i < 30; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("name", "name" + i);
            doc.set("surname", "surname" + i);
            doc.save();
        }
        database.commit();
        ResultSet result = database.query("sql", "select from " + className + " order by surname desc");
        printExecutionPlan(result);

        String lastSurname = null;
        for (int i = 0; i < 30; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Assertions.assertNotNull(item);
            String thisSurname = item.getProperty("surname");
            if (lastSurname != null) {
                Assertions.assertTrue(lastSurname.compareTo(thisSurname) >= 0);
            }
            lastSurname = thisSurname;
        }
        Assertions.assertFalse(result.hasNext());
        result.close();
    }

    @Test
    public void testSelectOrderByAsc() {
        String className = "testSelectOrderByAsc";
        database.getSchema().createDocumentType(className);
        database.begin();
        for (int i = 0; i < 30; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("name", "name" + i);
            doc.set("surname", "surname" + i);
            doc.save();
        }
        database.commit();
        ResultSet result = database.query("sql", "select from " + className + " order by surname asc");
        printExecutionPlan(result);

        String lastSurname = null;
        for (int i = 0; i < 30; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Assertions.assertNotNull(item);
            String thisSurname = item.getProperty("surname");
            if (lastSurname != null) {
                Assertions.assertTrue(lastSurname.compareTo(thisSurname) <= 0);
            }
            lastSurname = thisSurname;
        }
        Assertions.assertFalse(result.hasNext());
        result.close();
    }

    @Test
    public void testSelectOrderByMassiveAsc() {
        String className = "testSelectOrderByMassiveAsc";
        database.getSchema().createDocumentType(className);
        database.begin();
        for (int i = 0; i < 100000; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("name", "name" + i);
            doc.set("surname", "surname" + i % 100);
            doc.save();
        }
        database.commit();
        long begin = System.nanoTime();
        ResultSet result = database.query("sql", "select from " + className + " order by surname asc limit 100");
        //    System.out.println("elapsed: " + (System.nanoTime() - begin));
        printExecutionPlan(result);

        for (int i = 0; i < 100; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Assertions.assertNotNull(item);
            Assertions.assertEquals("surname0", item.getProperty("surname"));
        }
        Assertions.assertFalse(result.hasNext());
        result.close();
    }

    @Test
    public void testSelectOrderWithProjections() {
        String className = "testSelectOrderWithProjections";
        database.getSchema().createDocumentType(className);
        database.begin();
        for (int i = 0; i < 100; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("name", "name" + i % 10);
            doc.set("surname", "surname" + i % 10);
            doc.save();
        }
        database.commit();
        long begin = System.nanoTime();
        ResultSet result = database.query("sql", "select name from " + className + " order by surname asc");
        //    System.out.println("elapsed: " + (System.nanoTime() - begin));
        printExecutionPlan(result);

        String lastName = null;
        for (int i = 0; i < 100; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Assertions.assertNotNull(item);
            String name = item.getProperty("name");
            Assertions.assertNotNull(name);
            if (i > 0) {
                Assertions.assertTrue(name.compareTo(lastName) >= 0);
            }
            lastName = name;
        }
        Assertions.assertFalse(result.hasNext());
        result.close();
    }

    @Test
    public void testSelectOrderWithProjections2() {
        String className = "testSelectOrderWithProjections2";
        database.getSchema().createDocumentType(className);
        database.begin();
        for (int i = 0; i < 100; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("name", "name" + i % 10);
            doc.set("surname", "surname" + i % 10);
            doc.save();
        }
        database.commit();
        long begin = System.nanoTime();
        ResultSet result =
                database.query("sql", "select name from " + className + " order by name asc, surname asc");
        //    System.out.println("elapsed: " + (System.nanoTime() - begin));
        printExecutionPlan(result);

        String lastName = null;
        for (int i = 0; i < 100; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Assertions.assertNotNull(item);
            String name = item.getProperty("name");
            Assertions.assertNotNull(name);
            if (i > 0) {
                Assertions.assertTrue(name.compareTo(lastName) >= 0);
            }
            lastName = name;
        }
        Assertions.assertFalse(result.hasNext());
        result.close();
    }

    @Test
    public void testSelectFullScanWithFilter1() {
        String className = "testSelectFullScanWithFilter1";
        database.getSchema().createDocumentType(className);
        database.begin();
        for (int i = 0; i < 300; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("name", "name" + i);
            doc.set("surname", "surname" + i);
            doc.save();
        }
        database.commit();
        ResultSet result =
                database.query("sql", "select from " + className + " where name = 'name1' or name = 'name7' ");
        printExecutionPlan(result);

        for (int i = 0; i < 2; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Assertions.assertNotNull(item);
            Object name = item.getProperty("name");
            Assertions.assertTrue("name1".equals(name) || "name7".equals(name));
        }
        Assertions.assertFalse(result.hasNext());
        result.close();
    }

    @Test
    public void testSelectFullScanWithFilter2() {
        String className = "testSelectFullScanWithFilter2";
        database.getSchema().createDocumentType(className);
        database.begin();
        for (int i = 0; i < 300; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("name", "name" + i);
            doc.set("surname", "surname" + i);
            doc.save();
        }
        database.commit();
        ResultSet result = database.query("sql", "select from " + className + " where name <> 'name1' ");
        printExecutionPlan(result);

        for (int i = 0; i < 299; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Assertions.assertNotNull(item);
            Object name = item.getProperty("name");
            Assertions.assertFalse("name1".equals(name));
        }
        Assertions.assertFalse(result.hasNext());
        result.close();
    }

    @Test
    public void testProjections() {
        String className = "testProjections";
        database.getSchema().createDocumentType(className);
        database.begin();
        for (int i = 0; i < 300; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("name", "name" + i);
            doc.set("surname", "surname" + i);
            doc.save();
        }
        database.commit();
        ResultSet result = database.query("sql", "select name from " + className);
        printExecutionPlan(result);

        for (int i = 0; i < 300; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Assertions.assertNotNull(item);
            String name = item.getProperty("name");
            String surname = item.getProperty("surname");
            Assertions.assertNotNull(name);
            Assertions.assertTrue(name.startsWith("name"));
            Assertions.assertNull(surname);
            Assertions.assertFalse(item.getElement().isPresent());
        }
        Assertions.assertFalse(result.hasNext());
        result.close();
    }

    @Test
    public void testCountStar() {
        String className = "testCountStar";
        database.getSchema().createDocumentType(className);

        database.begin();
        for (int i = 0; i < 7; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.save();
        }
        database.commit();
        try {
            ResultSet result = database.query("sql", "select count(*) from " + className);
            printExecutionPlan(result);
            Assertions.assertNotNull(result);
            Assertions.assertTrue(result.hasNext());
            Result next = result.next();
            Assertions.assertNotNull(next);
            Assertions.assertEquals(7L, (Object) next.getProperty("count(*)"));
            Assertions.assertFalse(result.hasNext());
            result.close();
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }
    }

    @Test
    public void testCountStar2() {
        String className = "testCountStar2";
        database.getSchema().createDocumentType(className);

        database.begin();
        for (int i = 0; i < 10; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("name", "name" + (i % 5));
            doc.save();
        }
        database.commit();
        try {
            ResultSet result = database.query("sql", "select count(*), name from " + className + " group by name");
            printExecutionPlan(result);
            Assertions.assertNotNull(result);
            for (int i = 0; i < 5; i++) {
                Assertions.assertTrue(result.hasNext());
                Result next = result.next();
                Assertions.assertNotNull(next);
                Assertions.assertEquals(2L, (Object) next.getProperty("count(*)"));
            }
            Assertions.assertFalse(result.hasNext());
            result.close();
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }
    }

    @Test
    public void testCountStarEmptyNoIndex() {
        String className = "testCountStarEmptyNoIndex";
        database.getSchema().createDocumentType(className);

        database.begin();
        MutableDocument elem = database.newDocument(className);
        elem.set("name", "bar");
        elem.save();
        database.commit();

        try {
            ResultSet result = database.query("sql", "select count(*) from " + className + " where name = 'foo'");
            printExecutionPlan(result);
            Assertions.assertNotNull(result);
            Assertions.assertTrue(result.hasNext());
            Result next = result.next();
            Assertions.assertNotNull(next);
            Assertions.assertEquals(0L, (Object) next.getProperty("count(*)"));
            Assertions.assertFalse(result.hasNext());
            result.close();
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }
    }

    @Test
    public void testCountStarEmptyNoIndexWithAlias() {
        String className = "testCountStarEmptyNoIndexWithAlias";
        database.getSchema().createDocumentType(className);

        database.begin();
        MutableDocument elem = database.newDocument(className);
        elem.set("name", "bar");
        elem.save();
        database.commit();

        try {
            ResultSet result =
                    database.query("sql", "select count(*) as a from " + className + " where name = 'foo'");
            printExecutionPlan(result);
            Assertions.assertNotNull(result);
            Assertions.assertTrue(result.hasNext());
            Result next = result.next();
            Assertions.assertNotNull(next);
            Assertions.assertEquals(0L, (Object) next.getProperty("a"));
            Assertions.assertFalse(result.hasNext());
            result.close();
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }
    }

    @Test
    public void testAggretateMixedWithNonAggregate() {
        String className = "testAggretateMixedWithNonAggregate";
        database.getSchema().createDocumentType(className);

        try {
            database.query("sql",
                            "select max(a) + max(b) + pippo + pluto as foo, max(d) + max(e), f from " + className)
                    .close();
            Assertions.fail();
        } catch (CommandExecutionException x) {

        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void testAggretateMixedWithNonAggregateInCollection() {
        String className = "testAggretateMixedWithNonAggregateInCollection";
        database.getSchema().createDocumentType(className);

        try {
            database.query("sql", "select [max(a), max(b), foo] from " + className).close();
            Assertions.fail();
        } catch (CommandExecutionException x) {

        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void testAggretateInCollection() {
        String className = "testAggretateInCollection";
        database.getSchema().createDocumentType(className);

        try {
            String query = "select [max(a), max(b)] from " + className;
            ResultSet result = database.query("sql", query);
            printExecutionPlan(query, result);
            result.close();
        } catch (Exception x) {
            Assertions.fail();
        }
    }

    @Test
    public void testAggretateMixedWithNonAggregateConstants() {
        String className = "testAggretateMixedWithNonAggregateConstants";
        database.getSchema().createDocumentType(className);

        try {
            ResultSet result =
                    database.query("sql",
                            "select max(a + b) + (max(b + c * 2) + 1 + 2) * 3 as foo, max(d) + max(e), f from "
                                    + className);
            printExecutionPlan(result);
            result.close();
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }
    }

    @Test
    public void testAggregateSum() {
        String className = "testAggregateSum";
        database.getSchema().createDocumentType(className);
        database.begin();
        for (int i = 0; i < 10; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("name", "name" + i);
            doc.set("val", i);
            doc.save();
        }
        database.commit();
        ResultSet result = database.query("sql", "select sum(val) from " + className);
        printExecutionPlan(result);
        Assertions.assertTrue(result.hasNext());
        Result item = result.next();
        Assertions.assertNotNull(item);
        Assertions.assertEquals(45, (Object) item.getProperty("sum(val)"));

        result.close();
    }

    @Test
    public void testAggregateSumGroupBy() {
        String className = "testAggregateSumGroupBy";
        database.getSchema().createDocumentType(className);
        database.begin();
        for (int i = 0; i < 10; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("type", i % 2 == 0 ? "even" : "odd");
            doc.set("val", i);
            doc.save();
        }
        database.commit();
        ResultSet result = database.query("sql", "select sum(val), type from " + className + " group by type");
        printExecutionPlan(result);
        boolean evenFound = false;
        boolean oddFound = false;
        for (int i = 0; i < 2; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Assertions.assertNotNull(item);
            if ("even".equals(item.getProperty("type"))) {
                Assertions.assertEquals(20, item.<Object>getProperty("sum(val)"));
                evenFound = true;
            } else if ("odd".equals(item.getProperty("type"))) {
                Assertions.assertEquals(25, item.<Object>getProperty("sum(val)"));
                oddFound = true;
            }
        }
        Assertions.assertFalse(result.hasNext());
        Assertions.assertTrue(evenFound);
        Assertions.assertTrue(oddFound);
        result.close();
    }

    @Test
    public void testAggregateSumMaxMinGroupBy() {
        String className = "testAggregateSumMaxMinGroupBy";
        database.getSchema().createDocumentType(className);
        database.begin();
        for (int i = 0; i < 10; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("type", i % 2 == 0 ? "even" : "odd");
            doc.set("val", i);
            doc.save();
        }
        database.commit();
        ResultSet result =
                database.query("sql", "select sum(val), max(val), min(val), type from " + className + " group by type");
        printExecutionPlan(result);
        boolean evenFound = false;
        boolean oddFound = false;
        for (int i = 0; i < 2; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Assertions.assertNotNull(item);
            if ("even".equals(item.getProperty("type"))) {
                Assertions.assertEquals(20, item.<Object>getProperty("sum(val)"));
                Assertions.assertEquals(8, item.<Object>getProperty("max(val)"));
                Assertions.assertEquals(0, item.<Object>getProperty("min(val)"));
                evenFound = true;
            } else if ("odd".equals(item.getProperty("type"))) {
                Assertions.assertEquals(25, item.<Object>getProperty("sum(val)"));
                Assertions.assertEquals(9, item.<Object>getProperty("max(val)"));
                Assertions.assertEquals(1, item.<Object>getProperty("min(val)"));
                oddFound = true;
            }
        }
        Assertions.assertFalse(result.hasNext());
        Assertions.assertTrue(evenFound);
        Assertions.assertTrue(oddFound);
        result.close();
    }

    @Test
    public void testAggregateSumNoGroupByInProjection() {
        String className = "testAggregateSumNoGroupByInProjection";
        database.getSchema().createDocumentType(className);
        database.begin();
        for (int i = 0; i < 10; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("type", i % 2 == 0 ? "even" : "odd");
            doc.set("val", i);
            doc.save();
        }
        database.commit();
        ResultSet result = database.query("sql", "select sum(val) from " + className + " group by type");
        printExecutionPlan(result);
        boolean evenFound = false;
        boolean oddFound = false;
        for (int i = 0; i < 2; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Assertions.assertNotNull(item);
            Object sum = item.getProperty("sum(val)");
            if (sum.equals(20)) {
                evenFound = true;
            } else if (sum.equals(25)) {
                oddFound = true;
            }
        }
        Assertions.assertFalse(result.hasNext());
        Assertions.assertTrue(evenFound);
        Assertions.assertTrue(oddFound);
        result.close();
    }

    @Test
    public void testAggregateSumNoGroupByInProjection2() {
        String className = "testAggregateSumNoGroupByInProjection2";
        database.getSchema().createDocumentType(className);
        database.begin();
        for (int i = 0; i < 10; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("type", i % 2 == 0 ? "dd1" : "dd2");
            doc.set("val", i);
            doc.save();
        }
        database.commit();
        ResultSet result =
                database.query("sql", "select sum(val) from " + className + " group by type.substring(0,1)");
        printExecutionPlan(result);
        for (int i = 0; i < 1; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Assertions.assertNotNull(item);
            Object sum = item.getProperty("sum(val)");
            Assertions.assertEquals(45, sum);
        }
        Assertions.assertFalse(result.hasNext());
        result.close();
    }

    @Test
    public void testFetchFromBucketNumber() {
        String className = "testFetchFromBucketNumber";
        Schema schema = database.getSchema();
        DocumentType clazz = schema.createDocumentType(className);
        String targetClusterName = clazz.getBuckets(false).get(0).getName();

        database.begin();
        for (int i = 0; i < 10; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("val", i);
            doc.save(targetClusterName);
        }
        database.commit();
        ResultSet result = database.query("sql", "select from bucket:" + targetClusterName);
        printExecutionPlan(result);
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Integer val = item.getProperty("val");
            Assertions.assertNotNull(val);
            sum += val;
        }
        Assertions.assertEquals(45, sum);
        Assertions.assertFalse(result.hasNext());
        result.close();
    }

    @Test
    public void testFetchFromBucketNumberOrderByRidDesc() {
        String className = "testFetchFromBucketNumberOrderByRidDesc";
        Schema schema = database.getSchema();
        DocumentType clazz = schema.createDocumentType(className);

        String targetBucketName = clazz.getBuckets(false).get(0).getName();

        database.begin();
        for (int i = 0; i < 10; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("val", i);
            doc.save(targetBucketName);
        }
        database.commit();
        ResultSet result =
                database.query("sql", "select from bucket:" + targetBucketName + " order by @rid desc");
        printExecutionPlan(result);
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Integer val = item.getProperty("val");
            Assertions.assertEquals(i, 9 - val);
        }

        Assertions.assertFalse(result.hasNext());
        result.close();
    }

    @Test
    public void testFetchFromClusterNumberOrderByRidAsc() {
        String className = "testFetchFromClusterNumberOrderByRidAsc";
        Schema schema = database.getSchema();
        DocumentType clazz = schema.createDocumentType(className);

        String targetClusterName = clazz.getBuckets(false).get(0).getName();

        database.begin();
        for (int i = 0; i < 10; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("val", i);
            doc.save(targetClusterName);
        }
        database.commit();
        ResultSet result = database.query("sql", "select from bucket:" + targetClusterName + " order by @rid asc");
        printExecutionPlan(result);
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Integer val = item.getProperty("val");
            Assertions.assertEquals((Object) i, val);
        }

        Assertions.assertFalse(result.hasNext());
        result.close();
    }

    @Test
    public void testFetchFromClustersNumberOrderByRidAsc() {
        String className = "testFetchFromClustersNumberOrderByRidAsc";
        Schema schema = database.getSchema();
        DocumentType clazz = schema.createDocumentType(className);
        if (clazz.getBuckets(false).size() < 2) {
            //clazz.addCluster("testFetchFromClustersNumberOrderByRidAsc_2");
            return; //TODO
        }

        String targetClusterName = clazz.getBuckets(false).get(0).getName();
        String targetClusterName2 = clazz.getBuckets(false).get(1).getName();

        database.begin();
        for (int i = 0; i < 10; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("val", i);
            doc.save(targetClusterName);
        }
        for (int i = 0; i < 10; i++) {
            MutableDocument doc = database.newDocument(className);
            doc.set("val", i);
            doc.save(targetClusterName2);
        }
        database.commit();

        ResultSet result =
                database.query("sql",
                        "select from bucket:["
                                + targetClusterName
                                + ", "
                                + targetClusterName2
                                + "] order by @rid asc");
        printExecutionPlan(result);

        for (int i = 0; i < 20; i++) {
            Assertions.assertTrue(result.hasNext());
            Result item = result.next();
            Integer val = item.getProperty("val");
            Assertions.assertEquals((Object) (i % 10), val);
        }

        Assertions.assertFalse(result.hasNext());
        result.close();
    }

//    @Test
//    public void testQueryAsTarget() {
//        String className = "testQueryAsTarget";
//        OSchema schema = database.getSchema();
//        OClass clazz = schema.createDocumentType(className);
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("val", i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select from (select from " + className + " where val > 2)  where val < 8");
//        printExecutionPlan(result);
//
//        for (int i = 0; i < 5; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Integer val = item.getProperty("val");
//            Assertions.assertTrue(val > 2);
//            Assertions.assertTrue(val < 8);
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testQuerySchema() {
//        ResultSet result = database.query("sql", "select from metadata:schema");
//        printExecutionPlan(result);
//
//        for (int i = 0; i < 1; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item.getProperty("classes"));
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testQueryMetadataIndexManager() {
//        ResultSet result = database.query("sql", "select from metadata:indexmanager");
//        printExecutionPlan(result);
//        for (int i = 0; i < 1; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item.getProperty("indexes"));
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testQueryMetadataIndexManager2() {
//        ResultSet result = database.query("sql", "select expand(indexes) from metadata:indexmanager");
//        printExecutionPlan(result);
//        Assertions.assertTrue(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testQueryMetadataDatabase() {
//        ResultSet result = database.query("sql", "select from metadata:database");
//        printExecutionPlan(result);
//
//        Assertions.assertTrue(result.hasNext());
//        Result item = result.next();
//        Assertions.assertEquals(
//                OSelectStatementExecutionTest.class.getSimpleName(), item.getProperty("name"));
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testQueryMetadataStorage() {
//        ResultSet result = database.query("sql", "select from metadata:storage");
//        printExecutionPlan(result);
//
//        Assertions.assertTrue(result.hasNext());
//        Result item = result.next();
//        Assertions.assertEquals(
//                OSelectStatementExecutionTest.class.getSimpleName(), item.getProperty("name"));
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testNonExistingRids() {
//        ResultSet result = database.query("sql", "select from #0:100000000");
//        printExecutionPlan(result);
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromSingleRid() {
//        ResultSet result = database.query("sql", "select from #0:1");
//        printExecutionPlan(result);
//        Assertions.assertTrue(result.hasNext());
//        Assertions.assertNotNull(result.next());
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromSingleRid2() {
//        ResultSet result = database.query("sql", "select from [#0:1]");
//        printExecutionPlan(result);
//        Assertions.assertTrue(result.hasNext());
//        Assertions.assertNotNull(result.next());
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromSingleRidParam() {
//        ResultSet result = database.query("sql", "select from ?", new ORecordId(0, 1));
//        printExecutionPlan(result);
//        Assertions.assertTrue(result.hasNext());
//        Assertions.assertNotNull(result.next());
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromSingleRid3() {
//        MutableDocument document = new MutableDocument();
//        document.save(db.getClusterNameById(0));
//
//        ResultSet result = database.query("sql", "select from [#0:1, #0:2]");
//        printExecutionPlan(result);
//        Assertions.assertTrue(result.hasNext());
//        Assertions.assertNotNull(result.next());
//        Assertions.assertTrue(result.hasNext());
//        Assertions.assertNotNull(result.next());
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromSingleRid4() {
//        MutableDocument document = new MutableDocument();
//        document.save(db.getClusterNameById(0));
//
//        ResultSet result = database.query("sql", "select from [#0:1, #0:2, #0:100000]");
//        printExecutionPlan(result);
//        Assertions.assertTrue(result.hasNext());
//        Assertions.assertNotNull(result.next());
//        Assertions.assertTrue(result.hasNext());
//        Assertions.assertNotNull(result.next());
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromClassWithIndex() {
//        String className = "testFetchFromClassWithIndex";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createIndex(className + ".name", OClass.INDEX_TYPE.NOTUNIQUE, "name");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.save();
//        }
//
//        ResultSet result = database.query("sql", "select from " + className + " where name = 'name2'");
//        printExecutionPlan(result);
//
//        Assertions.assertTrue(result.hasNext());
//        Result next = result.next();
//        Assertions.assertNotNull(next);
//        Assertions.assertEquals("name2", next.getProperty("name"));
//
//        Assertions.assertFalse(result.hasNext());
//
//        Optional<OExecutionPlan> p = result.getExecutionPlan();
//        Assertions.assertTrue(p.isPresent());
//        OExecutionPlan p2 = p.get();
//        Assertions.assertTrue(p2 instanceof OSelectExecutionPlan);
//        OSelectExecutionPlan plan = (OSelectExecutionPlan) p2;
//        Assertions.assertEquals(FetchFromIndexStep.class, plan.getSteps().get(0).getClass());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromIndex() {
//        boolean oldAllowManual = OGlobalConfiguration.INDEX_ALLOW_MANUAL_INDEXES.getValueAsBoolean();
//        OGlobalConfiguration.INDEX_ALLOW_MANUAL_INDEXES.setValue(true);
//        String className = "testFetchFromIndex";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        String indexName = className + ".name";
//        clazz.createIndex(indexName, OClass.INDEX_TYPE.NOTUNIQUE, "name");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.save();
//        }
//
//        ResultSet result = database.query("sql", "select from index:" + indexName + " where key = 'name2'");
//        printExecutionPlan(result);
//
//        Assertions.assertTrue(result.hasNext());
//        Result next = result.next();
//        Assertions.assertNotNull(next);
//
//        Assertions.assertFalse(result.hasNext());
//
//        Optional<OExecutionPlan> p = result.getExecutionPlan();
//        Assertions.assertTrue(p.isPresent());
//        OExecutionPlan p2 = p.get();
//        Assertions.assertTrue(p2 instanceof OSelectExecutionPlan);
//        OSelectExecutionPlan plan = (OSelectExecutionPlan) p2;
//        Assertions.assertEquals(FetchFromIndexStep.class, plan.getSteps().get(0).getClass());
//        result.close();
//        OGlobalConfiguration.INDEX_ALLOW_MANUAL_INDEXES.setValue(oldAllowManual);
//    }
//
//    @Test
//    public void testFetchFromClassWithIndexes() {
//        String className = "testFetchFromClassWithIndexes";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        clazz.createIndex(className + ".name", OClass.INDEX_TYPE.NOTUNIQUE, "name");
//        clazz.createIndex(className + ".surname", OClass.INDEX_TYPE.NOTUNIQUE, "surname");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select from " + className + " where name = 'name2' or surname = 'surname3'");
//        printExecutionPlan(result);
//
//        Assertions.assertTrue(result.hasNext());
//        for (int i = 0; i < 2; i++) {
//            Result next = result.next();
//            Assertions.assertNotNull(next);
//            Assertions.assertTrue(
//                    "name2".equals(next.getProperty("name"))
//                            || ("surname3".equals(next.getProperty("surname"))));
//        }
//
//        Assertions.assertFalse(result.hasNext());
//
//        Optional<OExecutionPlan> p = result.getExecutionPlan();
//        Assertions.assertTrue(p.isPresent());
//        OExecutionPlan p2 = p.get();
//        Assertions.assertTrue(p2 instanceof OSelectExecutionPlan);
//        OSelectExecutionPlan plan = (OSelectExecutionPlan) p2;
//        Assertions.assertEquals(ParallelExecStep.class, plan.getSteps().get(0).getClass());
//        ParallelExecStep parallel = (ParallelExecStep) plan.getSteps().get(0);
//        Assertions.assertEquals(2, parallel.getSubExecutionPlans().size());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromClassWithIndexes2() {
//        String className = "testFetchFromClassWithIndexes2";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        clazz.createIndex(className + ".name", OClass.INDEX_TYPE.NOTUNIQUE, "name");
//        clazz.createIndex(className + ".surname", OClass.INDEX_TYPE.NOTUNIQUE, "surname");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query(
//                        "select from "
//                                + className
//                                + " where foo is not null and (name = 'name2' or surname = 'surname3')");
//        printExecutionPlan(result);
//
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromClassWithIndexes3() {
//        String className = "testFetchFromClassWithIndexes3";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        clazz.createIndex(className + ".name", OClass.INDEX_TYPE.NOTUNIQUE, "name");
//        clazz.createIndex(className + ".surname", OClass.INDEX_TYPE.NOTUNIQUE, "surname");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.set("foo", i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query(
//                        "select from "
//                                + className
//                                + " where foo < 100 and (name = 'name2' or surname = 'surname3')");
//        printExecutionPlan(result);
//
//        Assertions.assertTrue(result.hasNext());
//        for (int i = 0; i < 2; i++) {
//            Result next = result.next();
//            Assertions.assertNotNull(next);
//            Assertions.assertTrue(
//                    "name2".equals(next.getProperty("name"))
//                            || ("surname3".equals(next.getProperty("surname"))));
//        }
//
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromClassWithIndexes4() {
//        String className = "testFetchFromClassWithIndexes4";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        clazz.createIndex(className + ".name", OClass.INDEX_TYPE.NOTUNIQUE, "name");
//        clazz.createIndex(className + ".surname", OClass.INDEX_TYPE.NOTUNIQUE, "surname");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.set("foo", i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query(
//                        "select from "
//                                + className
//                                + " where foo < 100 and ((name = 'name2' and foo < 20) or surname = 'surname3') and ( 4<5 and foo < 50)");
//        printExecutionPlan(result);
//
//        Assertions.assertTrue(result.hasNext());
//        for (int i = 0; i < 2; i++) {
//            Result next = result.next();
//            Assertions.assertNotNull(next);
//            Assertions.assertTrue(
//                    "name2".equals(next.getProperty("name"))
//                            || ("surname3".equals(next.getProperty("surname"))));
//        }
//
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromClassWithIndexes5() {
//        String className = "testFetchFromClassWithIndexes5";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        clazz.createIndex(className + ".name_surname", OClass.INDEX_TYPE.NOTUNIQUE, "name", "surname");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.set("foo", i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select from " + className + " where name = 'name3' and surname >= 'surname1'");
//        printExecutionPlan(result);
//
//        Assertions.assertTrue(result.hasNext());
//        for (int i = 0; i < 1; i++) {
//            Result next = result.next();
//            Assertions.assertNotNull(next);
//            Assertions.assertEquals("name3", next.getProperty("name"));
//        }
//
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromClassWithIndexes6() {
//        String className = "testFetchFromClassWithIndexes6";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        clazz.createIndex(className + ".name_surname", OClass.INDEX_TYPE.NOTUNIQUE, "name", "surname");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.set("foo", i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select from " + className + " where name = 'name3' and surname > 'surname3'");
//        printExecutionPlan(result);
//
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromClassWithIndexes7() {
//        String className = "testFetchFromClassWithIndexes7";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        clazz.createIndex(className + ".name_surname", OClass.INDEX_TYPE.NOTUNIQUE, "name", "surname");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.set("foo", i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select from " + className + " where name = 'name3' and surname >= 'surname3'");
//        printExecutionPlan(result);
//        for (int i = 0; i < 1; i++) {
//            Result next = result.next();
//            Assertions.assertNotNull(next);
//            Assertions.assertEquals("name3", next.getProperty("name"));
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromClassWithIndexes8() {
//        String className = "testFetchFromClassWithIndexes8";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        clazz.createIndex(className + ".name_surname", OClass.INDEX_TYPE.NOTUNIQUE, "name", "surname");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.set("foo", i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select from " + className + " where name = 'name3' and surname < 'surname3'");
//        printExecutionPlan(result);
//
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromClassWithIndexes9() {
//        String className = "testFetchFromClassWithIndexes9";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        clazz.createIndex(className + ".name_surname", OClass.INDEX_TYPE.NOTUNIQUE, "name", "surname");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.set("foo", i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select from " + className + " where name = 'name3' and surname <= 'surname3'");
//        printExecutionPlan(result);
//        for (int i = 0; i < 1; i++) {
//            Result next = result.next();
//            Assertions.assertNotNull(next);
//            Assertions.assertEquals("name3", next.getProperty("name"));
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromClassWithIndexes10() {
//        String className = "testFetchFromClassWithIndexes10";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        clazz.createIndex(className + ".name_surname", OClass.INDEX_TYPE.NOTUNIQUE, "name", "surname");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.set("foo", i);
//            doc.save();
//        }
//
//        ResultSet result = database.query("sql", "select from " + className + " where name > 'name3' ");
//        printExecutionPlan(result);
//        for (int i = 0; i < 6; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result next = result.next();
//            Assertions.assertNotNull(next);
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromClassWithIndexes11() {
//        String className = "testFetchFromClassWithIndexes11";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        clazz.createIndex(className + ".name_surname", OClass.INDEX_TYPE.NOTUNIQUE, "name", "surname");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.set("foo", i);
//            doc.save();
//        }
//
//        ResultSet result = database.query("sql", "select from " + className + " where name >= 'name3' ");
//        printExecutionPlan(result);
//        for (int i = 0; i < 7; i++) {
//            Result next = result.next();
//            Assertions.assertNotNull(next);
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromClassWithIndexes12() {
//        String className = "testFetchFromClassWithIndexes12";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        clazz.createIndex(className + ".name_surname", OClass.INDEX_TYPE.NOTUNIQUE, "name", "surname");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.set("foo", i);
//            doc.save();
//        }
//
//        ResultSet result = database.query("sql", "select from " + className + " where name < 'name3' ");
//        printExecutionPlan(result);
//        for (int i = 0; i < 3; i++) {
//            Result next = result.next();
//            Assertions.assertNotNull(next);
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromClassWithIndexes13() {
//        String className = "testFetchFromClassWithIndexes13";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        clazz.createIndex(className + ".name_surname", OClass.INDEX_TYPE.NOTUNIQUE, "name", "surname");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.set("foo", i);
//            doc.save();
//        }
//
//        ResultSet result = database.query("sql", "select from " + className + " where name <= 'name3' ");
//        printExecutionPlan(result);
//        for (int i = 0; i < 4; i++) {
//            Result next = result.next();
//            Assertions.assertNotNull(next);
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromClassWithIndexes14() {
//        String className = "testFetchFromClassWithIndexes14";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        clazz.createIndex(className + ".name_surname", OClass.INDEX_TYPE.NOTUNIQUE, "name", "surname");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.set("foo", i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select from " + className + " where name > 'name3' and name < 'name5'");
//        printExecutionPlan(result);
//        for (int i = 0; i < 1; i++) {
//            Result next = result.next();
//            Assertions.assertNotNull(next);
//        }
//        Assertions.assertFalse(result.hasNext());
//        OSelectExecutionPlan plan = (OSelectExecutionPlan) result.getExecutionPlan().get();
//        Assertions.assertEquals(
//                1, plan.getSteps().stream().filter(step -> step instanceof FetchFromIndexStep).count());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromClassWithIndexes15() {
//        String className = "testFetchFromClassWithIndexes15";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        clazz.createIndex(className + ".name_surname", OClass.INDEX_TYPE.NOTUNIQUE, "name", "surname");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.set("foo", i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query(
//                        "select from "
//                                + className
//                                + " where name > 'name6' and name = 'name3' and surname > 'surname2' and surname < 'surname5' ");
//        printExecutionPlan(result);
//        Assertions.assertFalse(result.hasNext());
//        OSelectExecutionPlan plan = (OSelectExecutionPlan) result.getExecutionPlan().get();
//        Assertions.assertEquals(
//                1, plan.getSteps().stream().filter(step -> step instanceof FetchFromIndexStep).count());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromClassWithHashIndexes1() {
//        String className = "testFetchFromClassWithHashIndexes1";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        clazz.createIndex(
//                className + ".name_surname", OClass.INDEX_TYPE.NOTUNIQUE_HASH_INDEX, "name", "surname");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.set("foo", i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select from " + className + " where name = 'name6' and surname = 'surname6' ");
//        printExecutionPlan(result);
//
//        for (int i = 0; i < 1; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result next = result.next();
//            Assertions.assertNotNull(next);
//        }
//        Assertions.assertFalse(result.hasNext());
//        OSelectExecutionPlan plan = (OSelectExecutionPlan) result.getExecutionPlan().get();
//        Assertions.assertEquals(
//                1, plan.getSteps().stream().filter(step -> step instanceof FetchFromIndexStep).count());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromClassWithHashIndexes2() {
//        String className = "testFetchFromClassWithHashIndexes2";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        clazz.createIndex(
//                className + ".name_surname", OClass.INDEX_TYPE.NOTUNIQUE_HASH_INDEX, "name", "surname");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.set("foo", i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select from " + className + " where name = 'name6' and surname >= 'surname6' ");
//        printExecutionPlan(result);
//
//        for (int i = 0; i < 1; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result next = result.next();
//            Assertions.assertNotNull(next);
//        }
//        Assertions.assertFalse(result.hasNext());
//        OSelectExecutionPlan plan = (OSelectExecutionPlan) result.getExecutionPlan().get();
//        Assertions.assertEquals(
//                FetchFromClassExecutionStep.class, plan.getSteps().get(0).getClass()); // index not used
//        result.close();
//    }
//
//    @Test
//    public void testExpand1() {
//        String childClassName = "testExpand1_child";
//        String parentClassName = "testExpand1_parent";
//        OClass childClass = database.getSchema().createDocumentType(childClassName);
//        OClass parentClass = database.getSchema().createDocumentType(parentClassName);
//
//        int count = 10;
//        for (int i = 0; i < count; i++) {
//            MutableDocument doc = database.newDocument(childClassName);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.set("foo", i);
//            doc.save();
//
//            MutableDocument parent = new MutableDocument(parentClassName);
//            parent.setProperty("linked", doc);
//            parent.save();
//        }
//
//        ResultSet result = database.query("sql", "select expand(linked) from " + parentClassName);
//        printExecutionPlan(result);
//
//        for (int i = 0; i < count; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result next = result.next();
//            Assertions.assertNotNull(next);
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testExpand2() {
//        String childClassName = "testExpand2_child";
//        String parentClassName = "testExpand2_parent";
//        OClass childClass = database.getSchema().createDocumentType(childClassName);
//        OClass parentClass = database.getSchema().createDocumentType(parentClassName);
//
//        int count = 10;
//        int collSize = 11;
//        for (int i = 0; i < count; i++) {
//            List coll = new ArrayList<>();
//            for (int j = 0; j < collSize; j++) {
//                MutableDocument doc = database.newDocument(childClassName);
//                doc.set("name", "name" + i);
//                doc.save();
//                coll.add(doc);
//            }
//
//            MutableDocument parent = new MutableDocument(parentClassName);
//            parent.setProperty("linked", coll);
//            parent.save();
//        }
//
//        ResultSet result = database.query("sql", "select expand(linked) from " + parentClassName);
//        printExecutionPlan(result);
//
//        for (int i = 0; i < count * collSize; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result next = result.next();
//            Assertions.assertNotNull(next);
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testExpand3() {
//        String childClassName = "testExpand3_child";
//        String parentClassName = "testExpand3_parent";
//        OClass childClass = database.getSchema().createDocumentType(childClassName);
//        OClass parentClass = database.getSchema().createDocumentType(parentClassName);
//
//        int count = 30;
//        int collSize = 7;
//        for (int i = 0; i < count; i++) {
//            List coll = new ArrayList<>();
//            for (int j = 0; j < collSize; j++) {
//                MutableDocument doc = database.newDocument(childClassName);
//                doc.set("name", "name" + j);
//                doc.save();
//                coll.add(doc);
//            }
//
//            MutableDocument parent = new MutableDocument(parentClassName);
//            parent.setProperty("linked", coll);
//            parent.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select expand(linked) from " + parentClassName + " order by name");
//        printExecutionPlan(result);
//
//        String last = null;
//        for (int i = 0; i < count * collSize; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result next = result.next();
//            if (i > 0) {
//                Assertions.assertTrue(last.compareTo(next.getProperty("name")) <= 0);
//            }
//            last = next.getProperty("name");
//            Assertions.assertNotNull(next);
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testDistinct1() {
//        String className = "testDistinct1";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//
//        for (int i = 0; i < 30; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i % 10);
//            doc.set("surname", "surname" + i % 10);
//            doc.save();
//        }
//
//        ResultSet result = database.query("sql", "select distinct name, surname from " + className);
//        printExecutionPlan(result);
//
//        for (int i = 0; i < 10; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result next = result.next();
//            Assertions.assertNotNull(next);
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testDistinct2() {
//        String className = "testDistinct2";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//
//        for (int i = 0; i < 30; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i % 10);
//            doc.set("surname", "surname" + i % 10);
//            doc.save();
//        }
//
//        ResultSet result = database.query("sql", "select distinct(name) from " + className);
//        printExecutionPlan(result);
//
//        for (int i = 0; i < 10; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result next = result.next();
//            Assertions.assertNotNull(next);
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testLet1() {
//        ResultSet result = database.query("sql", "select $a as one, $b as two let $a = 1, $b = 1+1");
//        Assertions.assertTrue(result.hasNext());
//        Result item = result.next();
//        Assertions.assertNotNull(item);
//        Assertions.assertEquals(1, item.<Object>getProperty("one"));
//        Assertions.assertEquals(2, item.<Object>getProperty("two"));
//        printExecutionPlan(result);
//        result.close();
//    }
//
//    @Test
//    public void testLet1Long() {
//        ResultSet result = database.query("sql", "select $a as one, $b as two let $a = 1L, $b = 1L+1");
//        Assertions.assertTrue(result.hasNext());
//        Result item = result.next();
//        Assertions.assertNotNull(item);
//        Assertions.assertEquals(1l, item.<Object>getProperty("one"));
//        Assertions.assertEquals(2l, item.<Object>getProperty("two"));
//        printExecutionPlan(result);
//        result.close();
//    }
//
//    @Test
//    public void testLet2() {
//        ResultSet result = database.query("sql", "select $a as one let $a = (select 1 as a)");
//        printExecutionPlan(result);
//        Assertions.assertTrue(result.hasNext());
//        Result item = result.next();
//        Assertions.assertNotNull(item);
//        Object one = item.getProperty("one");
//        Assertions.assertTrue(one instanceof List);
//        Assertions.assertEquals(1, ((List) one).size());
//        Object x = ((List) one).get(0);
//        Assertions.assertTrue(x instanceof Result);
//        Assertions.assertEquals(1, (Object) ((Result) x).getProperty("a"));
//        result.close();
//    }
//
//    @Test
//    public void testLet3() {
//        ResultSet result = database.query("sql", "select $a[0].foo as one let $a = (select 1 as foo)");
//        printExecutionPlan(result);
//        Assertions.assertTrue(result.hasNext());
//        Result item = result.next();
//        Assertions.assertNotNull(item);
//        Object one = item.getProperty("one");
//        Assertions.assertEquals(1, one);
//        result.close();
//    }
//
//    @Test
//    public void testLet4() {
//        String className = "testLet4";
//        database.getSchema().createDocumentType(className);
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query(
//                        "select name, surname, $nameAndSurname as fullname from "
//                                + className
//                                + " let $nameAndSurname = name + ' ' + surname");
//        printExecutionPlan(result);
//        for (int i = 0; i < 10; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertEquals(
//                    item.getProperty("fullname"),
//                    item.getProperty("name") + " " + item.getProperty("surname"));
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testLet5() {
//        String className = "testLet5";
//        database.getSchema().createDocumentType(className);
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query(
//                        "select from "
//                                + className
//                                + " where name in (select name from "
//                                + className
//                                + " where name = 'name1')");
//        printExecutionPlan(result);
//        for (int i = 0; i < 1; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertEquals("name1", item.getProperty("name"));
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testLet6() {
//        String className = "testLet6";
//        database.getSchema().createDocumentType(className);
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query(
//                        "select $foo as name from "
//                                + className
//                                + " let $foo = (select name from "
//                                + className
//                                + " where name = $parent.$current.name)");
//        printExecutionPlan(result);
//        for (int i = 0; i < 10; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertNotNull(item.getProperty("name"));
//            Assertions.assertTrue(item.getProperty("name") instanceof Collection);
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testLet7() {
//        String className = "testLet7";
//        database.getSchema().createDocumentType(className);
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query(
//                        "select $bar as name from "
//                                + className
//                                + " "
//                                + "let $foo = (select name from "
//                                + className
//                                + " where name = $parent.$current.name),"
//                                + "$bar = $foo[0].name");
//        printExecutionPlan(result);
//        for (int i = 0; i < 10; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertNotNull(item.getProperty("name"));
//            Assertions.assertTrue(item.getProperty("name") instanceof String);
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testLetWithTraverseFunction() {
//        String vertexClassName = "testLetWithTraverseFunction";
//        String edgeClassName = "testLetWithTraverseFunctioEdge";
//
//        OClass vertexClass = database.createVertexClass(vertexClassName);
//
//        OVertex doc1 = database.newVertex(vertexClass);
//        doc1.setProperty("name", "A");
//        doc1.save();
//
//        OVertex doc2 = database.newVertex(vertexClass);
//        doc2.setProperty("name", "B");
//        doc2.save();
//        ORID doc2Id = doc2.getIdentity();
//
//        OClass edgeClass = database.createEdgeClass(edgeClassName);
//
//        database.newEdge(doc1, doc2, edgeClass);
//        String queryString =
//                "SELECT $x, name FROM " + vertexClassName + " let $x = out(\"" + edgeClassName + "\")";
//        ResultSet resultSet = database.query(queryString);
//        int counter = 0;
//        while (resultSet.hasNext()) {
//            Result result = resultSet.next();
//            Iterable edge = result.getProperty("$x");
//            Iterator<OIdentifiable> iter = edge.iterator();
//            while (iter.hasNext()) {
//                OVertex toVertex = database.load(iter.next().getIdentity());
//                if (doc2Id.equals(toVertex.getIdentity())) {
//                    ++counter;
//                }
//            }
//        }
//        Assertions.assertEquals(1, counter);
//        resultSet.close();
//    }
//
//    @Test
//    public void testUnwind1() {
//        String className = "testUnwind1";
//        database.getSchema().createDocumentType(className);
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("i", i);
//            doc.set("iSeq", new int[] {i, 2 * i, 4 * i});
//            doc.save();
//        }
//
//        ResultSet result = database.query("sql", "select i, iSeq from " + className + " unwind iSeq");
//        printExecutionPlan(result);
//        for (int i = 0; i < 30; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertNotNull(item.getProperty("i"));
//            Assertions.assertNotNull(item.getProperty("iSeq"));
//            Integer first = item.getProperty("i");
//            Integer second = item.getProperty("iSeq");
//            Assertions.assertTrue(first + second == 0 || second.intValue() % first.intValue() == 0);
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testUnwind2() {
//        String className = "testUnwind2";
//        database.getSchema().createDocumentType(className);
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("i", i);
//            List<Integer> iSeq = new ArrayList<>();
//            iSeq.add(i);
//            iSeq.add(i * 2);
//            iSeq.add(i * 4);
//            doc.set("iSeq", iSeq);
//            doc.save();
//        }
//
//        ResultSet result = database.query("sql", "select i, iSeq from " + className + " unwind iSeq");
//        printExecutionPlan(result);
//        for (int i = 0; i < 30; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertNotNull(item.getProperty("i"));
//            Assertions.assertNotNull(item.getProperty("iSeq"));
//            Integer first = item.getProperty("i");
//            Integer second = item.getProperty("iSeq");
//            Assertions.assertTrue(first + second == 0 || second.intValue() % first.intValue() == 0);
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromSubclassIndexes1() {
//        String parent = "testFetchFromSubclassIndexes1_parent";
//        String child1 = "testFetchFromSubclassIndexes1_child1";
//        String child2 = "testFetchFromSubclassIndexes1_child2";
//        OClass parentClass = database.getSchema().createDocumentType(parent);
//        OClass childClass1 = database.getSchema().createDocumentType(child1, parentClass);
//        OClass childClass2 = database.getSchema().createDocumentType(child2, parentClass);
//
//        parentClass.createProperty("name", OType.STRING);
//        childClass1.createIndex(child1 + ".name", OClass.INDEX_TYPE.NOTUNIQUE, "name");
//        childClass2.createIndex(child2 + ".name", OClass.INDEX_TYPE.NOTUNIQUE, "name");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(child1);
//            doc.set("name", "name" + i);
//            doc.save();
//        }
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(child2);
//            doc.set("name", "name" + i);
//            doc.save();
//        }
//
//        ResultSet result = database.query("sql", "select from " + parent + " where name = 'name1'");
//        printExecutionPlan(result);
//        OInternalExecutionPlan plan = (OInternalExecutionPlan) result.getExecutionPlan().get();
//        Assertions.assertTrue(plan.getSteps().get(0) instanceof ParallelExecStep);
//        for (int i = 0; i < 2; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromSubclassIndexes2() {
//        String parent = "testFetchFromSubclassIndexes2_parent";
//        String child1 = "testFetchFromSubclassIndexes2_child1";
//        String child2 = "testFetchFromSubclassIndexes2_child2";
//        OClass parentClass = database.getSchema().createDocumentType(parent);
//        OClass childClass1 = database.getSchema().createDocumentType(child1, parentClass);
//        OClass childClass2 = database.getSchema().createDocumentType(child2, parentClass);
//
//        parentClass.createProperty("name", OType.STRING);
//        childClass1.createIndex(child1 + ".name", OClass.INDEX_TYPE.NOTUNIQUE, "name");
//        childClass2.createIndex(child2 + ".name", OClass.INDEX_TYPE.NOTUNIQUE, "name");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(child1);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(child2);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select from " + parent + " where name = 'name1' and surname = 'surname1'");
//        printExecutionPlan(result);
//        OInternalExecutionPlan plan = (OInternalExecutionPlan) result.getExecutionPlan().get();
//        Assertions.assertTrue(plan.getSteps().get(0) instanceof ParallelExecStep);
//        for (int i = 0; i < 2; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromSubclassIndexes3() {
//        String parent = "testFetchFromSubclassIndexes3_parent";
//        String child1 = "testFetchFromSubclassIndexes3_child1";
//        String child2 = "testFetchFromSubclassIndexes3_child2";
//        OClass parentClass = database.getSchema().createDocumentType(parent);
//        OClass childClass1 = database.getSchema().createDocumentType(child1, parentClass);
//        OClass childClass2 = database.getSchema().createDocumentType(child2, parentClass);
//
//        parentClass.createProperty("name", OType.STRING);
//        childClass1.createIndex(child1 + ".name", OClass.INDEX_TYPE.NOTUNIQUE, "name");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(child1);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(child2);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select from " + parent + " where name = 'name1' and surname = 'surname1'");
//        printExecutionPlan(result);
//        OInternalExecutionPlan plan = (OInternalExecutionPlan) result.getExecutionPlan().get();
//        Assertions.assertTrue(
//                plan.getSteps().get(0) instanceof FetchFromClassExecutionStep); // no index used
//        for (int i = 0; i < 2; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromSubclassIndexes4() {
//        String parent = "testFetchFromSubclassIndexes4_parent";
//        String child1 = "testFetchFromSubclassIndexes4_child1";
//        String child2 = "testFetchFromSubclassIndexes4_child2";
//        OClass parentClass = database.getSchema().createDocumentType(parent);
//        OClass childClass1 = database.getSchema().createDocumentType(child1, parentClass);
//        OClass childClass2 = database.getSchema().createDocumentType(child2, parentClass);
//
//        parentClass.createProperty("name", OType.STRING);
//        childClass1.createIndex(child1 + ".name", OClass.INDEX_TYPE.NOTUNIQUE, "name");
//        childClass2.createIndex(child2 + ".name", OClass.INDEX_TYPE.NOTUNIQUE, "name");
//
//        MutableDocument parentdoc = database.newDocument(parent);
//        parentdoc.set("name", "foo");
//        parentdoc.save();
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(child1);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(child2);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select from " + parent + " where name = 'name1' and surname = 'surname1'");
//        printExecutionPlan(result);
//        OInternalExecutionPlan plan = (OInternalExecutionPlan) result.getExecutionPlan().get();
//        Assertions.assertTrue(
//                plan.getSteps().get(0)
//                        instanceof
//                        FetchFromClassExecutionStep); // no index, because the superclass is not empty
//        for (int i = 0; i < 2; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromSubSubclassIndexes() {
//        String parent = "testFetchFromSubSubclassIndexes_parent";
//        String child1 = "testFetchFromSubSubclassIndexes_child1";
//        String child2 = "testFetchFromSubSubclassIndexes_child2";
//        String child2_1 = "testFetchFromSubSubclassIndexes_child2_1";
//        String child2_2 = "testFetchFromSubSubclassIndexes_child2_2";
//        OClass parentClass = database.getSchema().createDocumentType(parent);
//        OClass childClass1 = database.getSchema().createDocumentType(child1, parentClass);
//        OClass childClass2 = database.getSchema().createDocumentType(child2, parentClass);
//        OClass childClass2_1 = database.getSchema().createDocumentType(child2_1, childClass2);
//        OClass childClass2_2 = database.getSchema().createDocumentType(child2_2, childClass2);
//
//        parentClass.createProperty("name", OType.STRING);
//        childClass1.createIndex(child1 + ".name", OClass.INDEX_TYPE.NOTUNIQUE, "name");
//        childClass2_1.createIndex(child2_1 + ".name", OClass.INDEX_TYPE.NOTUNIQUE, "name");
//        childClass2_2.createIndex(child2_2 + ".name", OClass.INDEX_TYPE.NOTUNIQUE, "name");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(child1);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(child2_1);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(child2_2);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select from " + parent + " where name = 'name1' and surname = 'surname1'");
//        printExecutionPlan(result);
//        OInternalExecutionPlan plan = (OInternalExecutionPlan) result.getExecutionPlan().get();
//        Assertions.assertTrue(plan.getSteps().get(0) instanceof ParallelExecStep);
//        for (int i = 0; i < 3; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testFetchFromSubSubclassIndexesWithDiamond() {
//        String parent = "testFetchFromSubSubclassIndexesWithDiamond_parent";
//        String child1 = "testFetchFromSubSubclassIndexesWithDiamond_child1";
//        String child2 = "testFetchFromSubSubclassIndexesWithDiamond_child2";
//        String child12 = "testFetchFromSubSubclassIndexesWithDiamond_child12";
//
//        OClass parentClass = database.getSchema().createDocumentType(parent);
//        OClass childClass1 = database.getSchema().createDocumentType(child1, parentClass);
//        OClass childClass2 = database.getSchema().createDocumentType(child2, parentClass);
//        OClass childClass12 =
//                database.getSchema().createDocumentType(child12, childClass1, childClass2);
//
//        parentClass.createProperty("name", OType.STRING);
//        childClass1.createIndex(child1 + ".name", OClass.INDEX_TYPE.NOTUNIQUE, "name");
//        childClass2.createIndex(child2 + ".name", OClass.INDEX_TYPE.NOTUNIQUE, "name");
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(child1);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(child2);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(child12);
//            doc.set("name", "name" + i);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select from " + parent + " where name = 'name1' and surname = 'surname1'");
//        printExecutionPlan(result);
//        OInternalExecutionPlan plan = (OInternalExecutionPlan) result.getExecutionPlan().get();
//        Assertions.assertTrue(plan.getSteps().get(0) instanceof FetchFromClassExecutionStep);
//        for (int i = 0; i < 3; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testIndexPlusSort1() {
//        String className = "testIndexPlusSort1";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        database.command(
//                        new OCommandSQL(
//                                "create index "
//                                        + className
//                                        + ".name_surname on "
//                                        + className
//                                        + " (name, surname) NOTUNIQUE"))
//                .execute();
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i % 3);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select from " + className + " where name = 'name1' order by surname ASC");
//        printExecutionPlan(result);
//        String lastSurname = null;
//        for (int i = 0; i < 3; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertNotNull(item.getProperty("surname"));
//
//            String surname = item.getProperty("surname");
//            if (i > 0) {
//                Assertions.assertTrue(surname.compareTo(lastSurname) > 0);
//            }
//            lastSurname = surname;
//        }
//        Assertions.assertFalse(result.hasNext());
//        OExecutionPlan plan = result.getExecutionPlan().get();
//        Assertions.assertEquals(
//                1, plan.getSteps().stream().filter(step -> step instanceof FetchFromIndexStep).count());
//        Assertions.assertEquals(
//                0, plan.getSteps().stream().filter(step -> step instanceof OrderByStep).count());
//        result.close();
//    }
//
//    @Test
//    public void testIndexPlusSort2() {
//        String className = "testIndexPlusSort2";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        database.command(
//                        new OCommandSQL(
//                                "create index "
//                                        + className
//                                        + ".name_surname on "
//                                        + className
//                                        + " (name, surname) NOTUNIQUE"))
//                .execute();
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i % 3);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select from " + className + " where name = 'name1' order by surname DESC");
//        printExecutionPlan(result);
//        String lastSurname = null;
//        for (int i = 0; i < 3; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertNotNull(item.getProperty("surname"));
//
//            String surname = item.getProperty("surname");
//            if (i > 0) {
//                Assertions.assertTrue(surname.compareTo(lastSurname) < 0);
//            }
//            lastSurname = surname;
//        }
//        Assertions.assertFalse(result.hasNext());
//        OExecutionPlan plan = result.getExecutionPlan().get();
//        Assertions.assertEquals(
//                1, plan.getSteps().stream().filter(step -> step instanceof FetchFromIndexStep).count());
//        Assertions.assertEquals(
//                0, plan.getSteps().stream().filter(step -> step instanceof OrderByStep).count());
//        result.close();
//    }
//
//    @Test
//    public void testIndexPlusSort3() {
//        String className = "testIndexPlusSort3";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        database.command(
//                        new OCommandSQL(
//                                "create index "
//                                        + className
//                                        + ".name_surname on "
//                                        + className
//                                        + " (name, surname) NOTUNIQUE"))
//                .execute();
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i % 3);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query(
//                        "select from " + className + " where name = 'name1' order by name DESC, surname DESC");
//        printExecutionPlan(result);
//        String lastSurname = null;
//        for (int i = 0; i < 3; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertNotNull(item.getProperty("surname"));
//
//            String surname = item.getProperty("surname");
//            if (i > 0) {
//                Assertions.assertTrue(((String) item.getProperty("surname")).compareTo(lastSurname) < 0);
//            }
//            lastSurname = surname;
//        }
//        Assertions.assertFalse(result.hasNext());
//        OExecutionPlan plan = result.getExecutionPlan().get();
//        Assertions.assertEquals(
//                1, plan.getSteps().stream().filter(step -> step instanceof FetchFromIndexStep).count());
//        Assertions.assertEquals(
//                0, plan.getSteps().stream().filter(step -> step instanceof OrderByStep).count());
//        result.close();
//    }
//
//    @Test
//    public void testIndexPlusSort4() {
//        String className = "testIndexPlusSort4";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        database.command(
//                        new OCommandSQL(
//                                "create index "
//                                        + className
//                                        + ".name_surname on "
//                                        + className
//                                        + " (name, surname) NOTUNIQUE"))
//                .execute();
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i % 3);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query(
//                        "select from " + className + " where name = 'name1' order by name ASC, surname ASC");
//        printExecutionPlan(result);
//        String lastSurname = null;
//        for (int i = 0; i < 3; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertNotNull(item.getProperty("surname"));
//
//            String surname = item.getProperty("surname");
//            if (i > 0) {
//                Assertions.assertTrue(surname.compareTo(lastSurname) > 0);
//            }
//            lastSurname = surname;
//        }
//        Assertions.assertFalse(result.hasNext());
//        OExecutionPlan plan = result.getExecutionPlan().get();
//        Assertions.assertEquals(
//                1, plan.getSteps().stream().filter(step -> step instanceof FetchFromIndexStep).count());
//        Assertions.assertEquals(
//                0, plan.getSteps().stream().filter(step -> step instanceof OrderByStep).count());
//        result.close();
//    }
//
//    @Test
//    public void testIndexPlusSort5() {
//        String className = "testIndexPlusSort5";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        clazz.createProperty("address", OType.STRING);
//        database.command(
//                        new OCommandSQL(
//                                "create index "
//                                        + className
//                                        + ".name_surname on "
//                                        + className
//                                        + " (name, surname, address) NOTUNIQUE"))
//                .execute();
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i % 3);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select from " + className + " where name = 'name1' order by surname ASC");
//        printExecutionPlan(result);
//        String lastSurname = null;
//        for (int i = 0; i < 3; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertNotNull(item.getProperty("surname"));
//            String surname = item.getProperty("surname");
//            if (i > 0) {
//                Assertions.assertTrue(surname.compareTo(lastSurname) > 0);
//            }
//            lastSurname = surname;
//        }
//        Assertions.assertFalse(result.hasNext());
//        OExecutionPlan plan = result.getExecutionPlan().get();
//        Assertions.assertEquals(
//                1, plan.getSteps().stream().filter(step -> step instanceof FetchFromIndexStep).count());
//        Assertions.assertEquals(
//                0, plan.getSteps().stream().filter(step -> step instanceof OrderByStep).count());
//        result.close();
//    }
//
//    @Test
//    public void testIndexPlusSort6() {
//        String className = "testIndexPlusSort6";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        clazz.createProperty("address", OType.STRING);
//        database.command(
//                        new OCommandSQL(
//                                "create index "
//                                        + className
//                                        + ".name_surname on "
//                                        + className
//                                        + " (name, surname, address) NOTUNIQUE"))
//                .execute();
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i % 3);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select from " + className + " where name = 'name1' order by surname DESC");
//        printExecutionPlan(result);
//        String lastSurname = null;
//        for (int i = 0; i < 3; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertNotNull(item.getProperty("surname"));
//            String surname = item.getProperty("surname");
//            if (i > 0) {
//                Assertions.assertTrue(surname.compareTo(lastSurname) < 0);
//            }
//            lastSurname = surname;
//        }
//        Assertions.assertFalse(result.hasNext());
//        OExecutionPlan plan = result.getExecutionPlan().get();
//        Assertions.assertEquals(
//                1, plan.getSteps().stream().filter(step -> step instanceof FetchFromIndexStep).count());
//        Assertions.assertEquals(
//                0, plan.getSteps().stream().filter(step -> step instanceof OrderByStep).count());
//        result.close();
//    }
//
//    @Test
//    public void testIndexPlusSort7() {
//        String className = "testIndexPlusSort7";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        clazz.createProperty("address", OType.STRING);
//        database.command(
//                        new OCommandSQL(
//                                "create index "
//                                        + className
//                                        + ".name_surname on "
//                                        + className
//                                        + " (name, surname, address) NOTUNIQUE"))
//                .execute();
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i % 3);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query("sql", "select from " + className + " where name = 'name1' order by address DESC");
//        printExecutionPlan(result);
//
//        for (int i = 0; i < 3; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertNotNull(item.getProperty("surname"));
//        }
//        Assertions.assertFalse(result.hasNext());
//        boolean orderStepFound = false;
//        for (OExecutionStep step : result.getExecutionPlan().get().getSteps()) {
//            if (step instanceof OrderByStep) {
//                orderStepFound = true;
//                break;
//            }
//        }
//        Assertions.assertTrue(orderStepFound);
//        result.close();
//    }
//
//    @Test
//    public void testIndexPlusSort8() {
//        String className = "testIndexPlusSort8";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        database.command(
//                        new OCommandSQL(
//                                "create index "
//                                        + className
//                                        + ".name_surname on "
//                                        + className
//                                        + " (name, surname) NOTUNIQUE"))
//                .execute();
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i % 3);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result =
//                database.query(
//                        "select from " + className + " where name = 'name1' order by name ASC, surname DESC");
//        printExecutionPlan(result);
//        for (int i = 0; i < 3; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertNotNull(item.getProperty("surname"));
//        }
//        Assertions.assertFalse(result.hasNext());
//        Assertions.assertFalse(result.hasNext());
//        boolean orderStepFound = false;
//        for (OExecutionStep step : result.getExecutionPlan().get().getSteps()) {
//            if (step instanceof OrderByStep) {
//                orderStepFound = true;
//                break;
//            }
//        }
//        Assertions.assertTrue(orderStepFound);
//        result.close();
//    }
//
//    @Test
//    public void testIndexPlusSort9() {
//        String className = "testIndexPlusSort9";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        database.command(
//                        new OCommandSQL(
//                                "create index "
//                                        + className
//                                        + ".name_surname on "
//                                        + className
//                                        + " (name, surname) NOTUNIQUE"))
//                .execute();
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i % 3);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result = database.query("sql", "select from " + className + " order by name , surname ASC");
//        printExecutionPlan(result);
//        for (int i = 0; i < 10; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertNotNull(item.getProperty("surname"));
//        }
//        Assertions.assertFalse(result.hasNext());
//        Assertions.assertFalse(result.hasNext());
//        boolean orderStepFound = false;
//        for (OExecutionStep step : result.getExecutionPlan().get().getSteps()) {
//            if (step instanceof OrderByStep) {
//                orderStepFound = true;
//                break;
//            }
//        }
//        Assertions.assertFalse(orderStepFound);
//        result.close();
//    }
//
//    @Test
//    public void testIndexPlusSort10() {
//        String className = "testIndexPlusSort10";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        database.command(
//                        new OCommandSQL(
//                                "create index "
//                                        + className
//                                        + ".name_surname on "
//                                        + className
//                                        + " (name, surname) NOTUNIQUE"))
//                .execute();
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i % 3);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result = database.query("sql", "select from " + className + " order by name desc, surname desc");
//        printExecutionPlan(result);
//        for (int i = 0; i < 10; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertNotNull(item.getProperty("surname"));
//        }
//        Assertions.assertFalse(result.hasNext());
//        Assertions.assertFalse(result.hasNext());
//        boolean orderStepFound = false;
//        for (OExecutionStep step : result.getExecutionPlan().get().getSteps()) {
//            if (step instanceof OrderByStep) {
//                orderStepFound = true;
//                break;
//            }
//        }
//        Assertions.assertFalse(orderStepFound);
//        result.close();
//    }
//
//    @Test
//    public void testIndexPlusSort11() {
//        String className = "testIndexPlusSort11";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        database.command(
//                        new OCommandSQL(
//                                "create index "
//                                        + className
//                                        + ".name_surname on "
//                                        + className
//                                        + " (name, surname) NOTUNIQUE"))
//                .execute();
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i % 3);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result = database.query("sql", "select from " + className + " order by name asc, surname desc");
//        printExecutionPlan(result);
//        for (int i = 0; i < 10; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertNotNull(item.getProperty("surname"));
//        }
//        Assertions.assertFalse(result.hasNext());
//        Assertions.assertFalse(result.hasNext());
//        boolean orderStepFound = false;
//        for (OExecutionStep step : result.getExecutionPlan().get().getSteps()) {
//            if (step instanceof OrderByStep) {
//                orderStepFound = true;
//                break;
//            }
//        }
//        Assertions.assertTrue(orderStepFound);
//        result.close();
//    }
//
//    @Test
//    public void testIndexPlusSort12() {
//        String className = "testIndexPlusSort12";
//        OClass clazz = database.getSchema().createDocumentType(className);
//        clazz.createProperty("name", OType.STRING);
//        clazz.createProperty("surname", OType.STRING);
//        database.command(
//                        new OCommandSQL(
//                                "create index "
//                                        + className
//                                        + ".name_surname on "
//                                        + className
//                                        + " (name, surname) NOTUNIQUE"))
//                .execute();
//
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i % 3);
//            doc.set("surname", "surname" + i);
//            doc.save();
//        }
//
//        ResultSet result = database.query("sql", "select from " + className + " order by name");
//        printExecutionPlan(result);
//        String last = null;
//        for (int i = 0; i < 10; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertNotNull(item.getProperty("name"));
//            String name = item.getProperty("name");
//            if (i > 0) {
//                Assertions.assertTrue(name.compareTo(last) >= 0);
//            }
//            last = name;
//        }
//        Assertions.assertFalse(result.hasNext());
//        Assertions.assertFalse(result.hasNext());
//        boolean orderStepFound = false;
//        for (OExecutionStep step : result.getExecutionPlan().get().getSteps()) {
//            if (step instanceof OrderByStep) {
//                orderStepFound = true;
//                break;
//            }
//        }
//        Assertions.assertFalse(orderStepFound);
//        result.close();
//    }
//
//    @Test
//    public void testSelectFromStringParam() {
//        String className = "testSelectFromStringParam";
//        database.getSchema().createDocumentType(className);
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.save();
//        }
//        ResultSet result = database.query("sql", "select from ?", className);
//        printExecutionPlan(result);
//
//        for (int i = 0; i < 10; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertTrue(("" + item.getProperty("name")).startsWith("name"));
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testSelectFromStringNamedParam() {
//        String className = "testSelectFromStringNamedParam";
//        database.getSchema().createDocumentType(className);
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.save();
//        }
//        Map<Object, Object> params = new HashMap<>();
//        params.put("target", className);
//        ResultSet result = database.query("sql", "select from :target", params);
//        printExecutionPlan(result);
//
//        for (int i = 0; i < 10; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertTrue(("" + item.getProperty("name")).startsWith("name"));
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testMatches() {
//        String className = "testMatches";
//        database.getSchema().createDocumentType(className);
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.save();
//        }
//        ResultSet result = database.query("sql", "select from " + className + " where name matches 'name1'");
//        printExecutionPlan(result);
//
//        for (int i = 0; i < 1; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Assertions.assertEquals(item.getProperty("name"), "name1");
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testRange() {
//        String className = "testRange";
//        database.getSchema().createDocumentType(className);
//
//        MutableDocument doc = database.newDocument(className);
//        doc.set("name", new String[] {"a", "b", "c", "d"});
//        doc.save();
//
//        ResultSet result = database.query("sql", "select name[0..3] as names from " + className);
//        printExecutionPlan(result);
//
//        for (int i = 0; i < 1; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Object names = item.getProperty("names");
//            if (names == null) {
//                Assertions.fail();
//            }
//            if (names instanceof Collection) {
//                Assertions.assertEquals(3, ((Collection) names).size());
//                Iterator iter = ((Collection) names).iterator();
//                Assertions.assertEquals("a", iter.next());
//                Assertions.assertEquals("b", iter.next());
//                Assertions.assertEquals("c", iter.next());
//            } else if (names.getClass().isArray()) {
//                Assertions.assertEquals(3, Array.getLength(names));
//            } else {
//                Assertions.fail();
//            }
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testRangeParams1() {
//        String className = "testRangeParams1";
//        database.getSchema().createDocumentType(className);
//
//        MutableDocument doc = database.newDocument(className);
//        doc.set("name", new String[] {"a", "b", "c", "d"});
//        doc.save();
//
//        ResultSet result = database.query("sql", "select name[?..?] as names from " + className, 0, 3);
//        printExecutionPlan(result);
//
//        for (int i = 0; i < 1; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Object names = item.getProperty("names");
//            if (names == null) {
//                Assertions.fail();
//            }
//            if (names instanceof Collection) {
//                Assertions.assertEquals(3, ((Collection) names).size());
//                Iterator iter = ((Collection) names).iterator();
//                Assertions.assertEquals("a", iter.next());
//                Assertions.assertEquals("b", iter.next());
//                Assertions.assertEquals("c", iter.next());
//            } else if (names.getClass().isArray()) {
//                Assertions.assertEquals(3, Array.getLength(names));
//            } else {
//                Assertions.fail();
//            }
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testRangeParams2() {
//        String className = "testRangeParams2";
//        database.getSchema().createDocumentType(className);
//
//        MutableDocument doc = database.newDocument(className);
//        doc.set("name", new String[] {"a", "b", "c", "d"});
//        doc.save();
//
//        Map<String, Object> params = new HashMap<>();
//        params.put("a", 0);
//        params.put("b", 3);
//        ResultSet result = database.query("sql", "select name[:a..:b] as names from " + className, params);
//        printExecutionPlan(result);
//
//        for (int i = 0; i < 1; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Object names = item.getProperty("names");
//            if (names == null) {
//                Assertions.fail();
//            }
//            if (names instanceof Collection) {
//                Assertions.assertEquals(3, ((Collection) names).size());
//                Iterator iter = ((Collection) names).iterator();
//                Assertions.assertEquals("a", iter.next());
//                Assertions.assertEquals("b", iter.next());
//                Assertions.assertEquals("c", iter.next());
//            } else if (names.getClass().isArray()) {
//                Assertions.assertEquals(3, Array.getLength(names));
//            } else {
//                Assertions.fail();
//            }
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testEllipsis() {
//        String className = "testEllipsis";
//        database.getSchema().createDocumentType(className);
//
//        MutableDocument doc = database.newDocument(className);
//        doc.set("name", new String[] {"a", "b", "c", "d"});
//        doc.save();
//
//        ResultSet result = database.query("sql", "select name[0...2] as names from " + className);
//        printExecutionPlan(result);
//
//        for (int i = 0; i < 1; i++) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertNotNull(item);
//            Object names = item.getProperty("names");
//            if (names == null) {
//                Assertions.fail();
//            }
//            if (names instanceof Collection) {
//                Assertions.assertEquals(3, ((Collection) names).size());
//                Iterator iter = ((Collection) names).iterator();
//                Assertions.assertEquals("a", iter.next());
//                Assertions.assertEquals("b", iter.next());
//                Assertions.assertEquals("c", iter.next());
//            } else if (names.getClass().isArray()) {
//                Assertions.assertEquals(3, Array.getLength(names));
//                Assertions.assertEquals("a", Array.get(names, 0));
//                Assertions.assertEquals("b", Array.get(names, 1));
//                Assertions.assertEquals("c", Array.get(names, 2));
//            } else {
//                Assertions.fail();
//            }
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testNewRid() {
//        ResultSet result = database.query("sql", "select {\"@rid\":\"#12:0\"} as theRid ");
//        Assertions.assertTrue(result.hasNext());
//        Result item = result.next();
//        Object rid = item.getProperty("theRid");
//        Assertions.assertTrue(rid instanceof OIdentifiable);
//        OIdentifiable id = (OIdentifiable) rid;
//        Assertions.assertEquals(12, id.getIdentity().getClusterId());
//        Assertions.assertEquals(0L, id.getIdentity().getClusterPosition());
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testNestedProjections1() {
//        String className = "testNestedProjections1";
//        database.command("create class " + className).close();
//        OElement elem1 = database.newElement(className);
//        elem1.setProperty("name", "a");
//        elem1.save();
//
//        OElement elem2 = database.newElement(className);
//        elem2.setProperty("name", "b");
//        elem2.setProperty("surname", "lkj");
//        elem2.save();
//
//        OElement elem3 = database.newElement(className);
//        elem3.setProperty("name", "c");
//        elem3.save();
//
//        OElement elem4 = database.newElement(className);
//        elem4.setProperty("name", "d");
//        elem4.setProperty("elem1", elem1);
//        elem4.setProperty("elem2", elem2);
//        elem4.setProperty("elem3", elem3);
//        elem4.save();
//
//        ResultSet result =
//                database.query(
//                        "select name, elem1:{*}, elem2:{!surname} from " + className + " where name = 'd'");
//        Assertions.assertTrue(result.hasNext());
//        Result item = result.next();
//        Assertions.assertNotNull(item);
//        // TODO refine this!
//        Assertions.assertTrue(item.getProperty("elem1") instanceof Result);
//        Assertions.assertEquals("a", ((Result) item.getProperty("elem1")).getProperty("name"));
//        printExecutionPlan(result);
//
//        result.close();
//    }
//
//    @Test
//    public void testSimpleCollectionFiltering() {
//        String className = "testSimpleCollectionFiltering";
//        database.command("create class " + className).close();
//        OElement elem1 = database.newElement(className);
//        List<String> coll = new ArrayList<>();
//        coll.add("foo");
//        coll.add("bar");
//        coll.add("baz");
//        elem1.setProperty("coll", coll);
//        elem1.save();
//
//        ResultSet result = database.query("sql", "select coll[='foo'] as filtered from " + className);
//        Assertions.assertTrue(result.hasNext());
//        Result item = result.next();
//        List res = item.getProperty("filtered");
//        Assertions.assertEquals(1, res.size());
//        Assertions.assertEquals("foo", res.get(0));
//        result.close();
//
//        result = database.query("sql", "select coll[<'ccc'] as filtered from " + className);
//        Assertions.assertTrue(result.hasNext());
//        item = result.next();
//        res = item.getProperty("filtered");
//        Assertions.assertEquals(2, res.size());
//        result.close();
//
//        result = database.query("sql", "select coll[LIKE 'ba%'] as filtered from " + className);
//        Assertions.assertTrue(result.hasNext());
//        item = result.next();
//        res = item.getProperty("filtered");
//        Assertions.assertEquals(2, res.size());
//        result.close();
//
//        result = database.query("sql", "select coll[in ['bar']] as filtered from " + className);
//        Assertions.assertTrue(result.hasNext());
//        item = result.next();
//        res = item.getProperty("filtered");
//        Assertions.assertEquals(1, res.size());
//        Assertions.assertEquals("bar", res.get(0));
//        result.close();
//    }
//
//    @Test
//    public void testContaninsWithConversion() {
//        String className = "testContaninsWithConversion";
//        database.command("create class " + className).close();
//        OElement elem1 = database.newElement(className);
//        List<Long> coll = new ArrayList<>();
//        coll.add(1L);
//        coll.add(3L);
//        coll.add(5L);
//        elem1.setProperty("coll", coll);
//        elem1.save();
//
//        OElement elem2 = database.newElement(className);
//        coll = new ArrayList<>();
//        coll.add(2L);
//        coll.add(4L);
//        coll.add(6L);
//        elem2.setProperty("coll", coll);
//        elem2.save();
//
//        ResultSet result = database.query("sql", "select from " + className + " where coll contains 1");
//        Assertions.assertTrue(result.hasNext());
//        result.next();
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//
//        result = database.query("sql", "select from " + className + " where coll contains 1L");
//        Assertions.assertTrue(result.hasNext());
//        result.next();
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//
//        result = database.query("sql", "select from " + className + " where coll contains 12L");
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testIndexPrefixUsage() {
//        // issue #7636
//        String className = "testIndexPrefixUsage";
//        database.command("create class " + className).close();
//        database.command("create property " + className + ".id LONG").close();
//        database.command("create property " + className + ".name STRING").close();
//        database.command("create index " + className + ".id_name on " + className + "(id, name) UNIQUE")
//                .close();
//        database.command("sql", "insert into " + className + " set id = 1 , name = 'Bar'").close();
//
//        ResultSet result = database.query("sql", "select from " + className + " where name = 'Bar'");
//        Assertions.assertTrue(result.hasNext());
//        result.next();
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testNamedParams() {
//        String className = "testNamedParams";
//        database.command("create class " + className).close();
//        database.command("sql", "insert into " + className + " set name = 'Foo', surname = 'Fox'").close();
//        database.command("sql", "insert into " + className + " set name = 'Bar', surname = 'Bax'").close();
//
//        Map<String, Object> params = new HashMap<>();
//        params.put("p1", "Foo");
//        params.put("p2", "Fox");
//        ResultSet result =
//                database.query("sql", "select from " + className + " where name = :p1 and surname = :p2", params);
//        Assertions.assertTrue(result.hasNext());
//        result.next();
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testNamedParamsWithIndex() {
//        String className = "testNamedParamsWithIndex";
//        database.command("create class " + className).close();
//        database.command("create property " + className + ".name STRING").close();
//        database.command("create index " + className + ".name ON " + className + " (name) NOTUNIQUE").close();
//        database.command("sql", "insert into " + className + " set name = 'Foo'").close();
//        database.command("sql", "insert into " + className + " set name = 'Bar'").close();
//
//        Map<String, Object> params = new HashMap<>();
//        params.put("p1", "Foo");
//        ResultSet result = database.query("sql", "select from " + className + " where name = :p1", params);
//        Assertions.assertTrue(result.hasNext());
//        result.next();
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testIsDefined() {
//        String className = "testIsDefined";
//        database.command("create class " + className).close();
//        database.command("sql", "insert into " + className + " set name = 'Foo'").close();
//        database.command("sql", "insert into " + className + " set sur = 'Bar'").close();
//        database.command("sql", "insert into " + className + " set sur = 'Barz'").close();
//
//        ResultSet result = database.query("sql", "select from " + className + " where name is defined");
//        Assertions.assertTrue(result.hasNext());
//        result.next();
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testIsNotDefined() {
//        String className = "testIsNotDefined";
//        database.command("create class " + className).close();
//        database.command("sql", "insert into " + className + " set name = 'Foo'").close();
//        database.command("sql", "insert into " + className + " set name = null, sur = 'Bar'").close();
//        database.command("sql", "insert into " + className + " set sur = 'Barz'").close();
//
//        ResultSet result = database.query("sql", "select from " + className + " where name is not defined");
//        Assertions.assertTrue(result.hasNext());
//        result.next();
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testRidPagination1() {
//        String className = "testRidPagination1";
//        OClass clazz = database.createClassIfNotExist(className);
//        int[] clusterIds = new int[clazz.getClusterIds().length];
//        if (clusterIds.length < 3) {
//            return;
//        }
//        System.arraycopy(clazz.getClusterIds(), 0, clusterIds, 0, clusterIds.length);
//        Arrays.sort(clusterIds);
//
//        for (int i = 0; i < clusterIds.length; i++) {
//            OElement elem = database.newElement(className);
//            elem.setProperty("cid", clusterIds[i]);
//            elem.save(db.getClusterNameById(clusterIds[i]));
//        }
//
//        ResultSet result =
//                database.query("sql", "select from " + className + " where @rid >= #" + clusterIds[1] + ":0");
//        OExecutionPlan execPlan = result.getExecutionPlan().get();
//        for (OExecutionStep oExecutionStep : execPlan.getSteps()) {
//            if (oExecutionStep instanceof FetchFromClassExecutionStep) {
//                Assertions.assertEquals(clusterIds.length, oExecutionStep.getSubSteps().size());
//                // clusters - 1 + fetch from tx...
//            }
//        }
//        int count = 0;
//        while (result.hasNext()) {
//            count++;
//            result.next();
//        }
//        result.close();
//        Assertions.assertEquals(clusterIds.length - 1, count);
//    }
//
//    @Test
//    public void testRidPagination2() {
//        String className = "testRidPagination2";
//        OClass clazz = database.createClassIfNotExist(className);
//        int[] clusterIds = new int[clazz.getClusterIds().length];
//        if (clusterIds.length < 3) {
//            return;
//        }
//        System.arraycopy(clazz.getClusterIds(), 0, clusterIds, 0, clusterIds.length);
//        Arrays.sort(clusterIds);
//
//        for (int i = 0; i < clusterIds.length; i++) {
//            OElement elem = database.newElement(className);
//            elem.setProperty("cid", clusterIds[i]);
//            elem.save(db.getClusterNameById(clusterIds[i]));
//        }
//
//        Map<String, Object> params = new HashMap<>();
//        params.put("rid", new ORecordId(clusterIds[1], 0));
//        ResultSet result = database.query("sql", "select from " + className + " where @rid >= :rid", params);
//        OExecutionPlan execPlan = result.getExecutionPlan().get();
//        for (OExecutionStep oExecutionStep : execPlan.getSteps()) {
//            if (oExecutionStep instanceof FetchFromClassExecutionStep) {
//                Assertions.assertEquals(clusterIds.length, oExecutionStep.getSubSteps().size());
//                // clusters - 1 + fetch from tx...
//            }
//        }
//        int count = 0;
//        while (result.hasNext()) {
//            count++;
//            result.next();
//        }
//        result.close();
//        Assertions.assertEquals(clusterIds.length - 1, count);
//    }
//
//    @Test
//    public void testContainsWithSubquery() {
//        String className = "testContainsWithSubquery";
//        OClass clazz1 = database.createClassIfNotExist(className + 1);
//        OClass clazz2 = database.createClassIfNotExist(className + 2);
//        clazz2.createProperty("tags", OType.EMBEDDEDLIST);
//
//        database.command("sql", "insert into " + className + 1 + "  set name = 'foo'");
//
//        database.command("sql", "insert into " + className + 2 + "  set tags = ['foo', 'bar']");
//        database.command("sql", "insert into " + className + 2 + "  set tags = ['baz', 'bar']");
//        database.command("sql", "insert into " + className + 2 + "  set tags = ['foo']");
//
//        try (ResultSet result =
//                     database.query(
//                             "select from "
//                                     + className
//                                     + 2
//                                     + " where tags contains (select from "
//                                     + className
//                                     + 1
//                                     + " where name = 'foo')")) {
//
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertFalse(result.hasNext());
//        }
//    }
//
//    @Test
//    public void testInWithSubquery() {
//        String className = "testInWithSubquery";
//        OClass clazz1 = database.createClassIfNotExist(className + 1);
//        OClass clazz2 = database.createClassIfNotExist(className + 2);
//        clazz2.createProperty("tags", OType.EMBEDDEDLIST);
//
//        database.command("sql", "insert into " + className + 1 + "  set name = 'foo'");
//
//        database.command("sql", "insert into " + className + 2 + "  set tags = ['foo', 'bar']");
//        database.command("sql", "insert into " + className + 2 + "  set tags = ['baz', 'bar']");
//        database.command("sql", "insert into " + className + 2 + "  set tags = ['foo']");
//
//        try (ResultSet result =
//                     database.query(
//                             "select from "
//                                     + className
//                                     + 2
//                                     + " where (select from "
//                                     + className
//                                     + 1
//                                     + " where name = 'foo') in tags")) {
//
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertFalse(result.hasNext());
//        }
//    }
//
//    @Test
//    public void testContainsAny() {
//        String className = "testContainsAny";
//        OClass clazz = database.createClassIfNotExist(className);
//        clazz.createProperty("tags", OType.EMBEDDEDLIST, OType.STRING);
//
//        database.command("sql", "insert into " + className + "  set tags = ['foo', 'bar']");
//        database.command("sql", "insert into " + className + "  set tags = ['bbb', 'FFF']");
//
//        try (ResultSet result =
//                     database.query("sql", "select from " + className + " where tags containsany ['foo','baz']")) {
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertFalse(result.hasNext());
//        }
//
//        try (ResultSet result =
//                     database.query("sql", "select from " + className + " where tags containsany ['foo','bar']")) {
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertFalse(result.hasNext());
//        }
//
//        try (ResultSet result =
//                     database.query("sql", "select from " + className + " where tags containsany ['foo','bbb']")) {
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertFalse(result.hasNext());
//        }
//
//        try (ResultSet result =
//                     database.query("sql", "select from " + className + " where tags containsany ['xx','baz']")) {
//            Assertions.assertFalse(result.hasNext());
//        }
//
//        try (ResultSet result = database.query("sql", "select from " + className + " where tags containsany []")) {
//            Assertions.assertFalse(result.hasNext());
//        }
//    }
//
//    @Test
//    public void testContainsAnyWithIndex() {
//        String className = "testContainsAnyWithIndex";
//        OClass clazz = database.createClassIfNotExist(className);
//        OProperty prop = clazz.createProperty("tags", OType.EMBEDDEDLIST, OType.STRING);
//        prop.createIndex(OClass.INDEX_TYPE.NOTUNIQUE);
//
//        database.command("sql", "insert into " + className + "  set tags = ['foo', 'bar']");
//        database.command("sql", "insert into " + className + "  set tags = ['bbb', 'FFF']");
//
//        try (ResultSet result =
//                     database.query("sql", "select from " + className + " where tags containsany ['foo','baz']")) {
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertFalse(result.hasNext());
//            Assertions.assertTrue(
//                    result.getExecutionPlan().get().getSteps().stream()
//                            .anyMatch(x -> x instanceof FetchFromIndexStep));
//        }
//
//        try (ResultSet result =
//                     database.query("sql", "select from " + className + " where tags containsany ['foo','bar']")) {
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertFalse(result.hasNext());
//            Assertions.assertTrue(
//                    result.getExecutionPlan().get().getSteps().stream()
//                            .anyMatch(x -> x instanceof FetchFromIndexStep));
//        }
//
//        try (ResultSet result =
//                     database.query("sql", "select from " + className + " where tags containsany ['foo','bbb']")) {
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertFalse(result.hasNext());
//            Assertions.assertTrue(
//                    result.getExecutionPlan().get().getSteps().stream()
//                            .anyMatch(x -> x instanceof FetchFromIndexStep));
//        }
//
//        try (ResultSet result =
//                     database.query("sql", "select from " + className + " where tags containsany ['xx','baz']")) {
//            Assertions.assertFalse(result.hasNext());
//            Assertions.assertTrue(
//                    result.getExecutionPlan().get().getSteps().stream()
//                            .anyMatch(x -> x instanceof FetchFromIndexStep));
//        }
//
//        try (ResultSet result = database.query("sql", "select from " + className + " where tags containsany []")) {
//            Assertions.assertFalse(result.hasNext());
//            Assertions.assertTrue(
//                    result.getExecutionPlan().get().getSteps().stream()
//                            .anyMatch(x -> x instanceof FetchFromIndexStep));
//        }
//    }
//
//    @Test
//    public void testContainsAll() {
//        String className = "testContainsAll";
//        OClass clazz = database.createClassIfNotExist(className);
//        clazz.createProperty("tags", OType.EMBEDDEDLIST, OType.STRING);
//
//        database.command("sql", "insert into " + className + "  set tags = ['foo', 'bar']");
//        database.command("sql", "insert into " + className + "  set tags = ['foo', 'FFF']");
//
//        try (ResultSet result =
//                     database.query("sql", "select from " + className + " where tags containsall ['foo','bar']")) {
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertFalse(result.hasNext());
//        }
//
//        try (ResultSet result =
//                     database.query("sql", "select from " + className + " where tags containsall ['foo']")) {
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertFalse(result.hasNext());
//        }
//    }
//
//    @Test
//    public void testBetween() {
//        String className = "testBetween";
//        OClass clazz = database.createClassIfNotExist(className);
//
//        database.command("sql", "insert into " + className + "  set name = 'foo1', val = 1");
//        database.command("sql", "insert into " + className + "  set name = 'foo2', val = 2");
//        database.command("sql", "insert into " + className + "  set name = 'foo3', val = 3");
//        database.command("sql", "insert into " + className + "  set name = 'foo4', val = 4");
//
//        try (ResultSet result = database.query("sql", "select from " + className + " where val between 2 and 3")) {
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertFalse(result.hasNext());
//        }
//    }
//
//    @Test
//    public void testInWithIndex() {
//        String className = "testInWithIndex";
//        OClass clazz = database.createClassIfNotExist(className);
//        OProperty prop = clazz.createProperty("tag", OType.STRING);
//        prop.createIndex(OClass.INDEX_TYPE.NOTUNIQUE);
//
//        database.command("sql", "insert into " + className + "  set tag = 'foo'");
//        database.command("sql", "insert into " + className + "  set tag = 'bar'");
//
//        try (ResultSet result = database.query("sql", "select from " + className + " where tag in ['foo','baz']")) {
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertFalse(result.hasNext());
//            Assertions.assertTrue(
//                    result.getExecutionPlan().get().getSteps().stream()
//                            .anyMatch(x -> x instanceof FetchFromIndexStep));
//        }
//
//        try (ResultSet result = database.query("sql", "select from " + className + " where tag in ['foo','bar']")) {
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertFalse(result.hasNext());
//            Assertions.assertTrue(
//                    result.getExecutionPlan().get().getSteps().stream()
//                            .anyMatch(x -> x instanceof FetchFromIndexStep));
//        }
//
//        try (ResultSet result = database.query("sql", "select from " + className + " where tag in []")) {
//            Assertions.assertFalse(result.hasNext());
//            Assertions.assertTrue(
//                    result.getExecutionPlan().get().getSteps().stream()
//                            .anyMatch(x -> x instanceof FetchFromIndexStep));
//        }
//
//        List<String> params = new ArrayList<>();
//        params.add("foo");
//        params.add("bar");
//        try (ResultSet result = database.query("sql", "select from " + className + " where tag in (?)", params)) {
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertFalse(result.hasNext());
//            Assertions.assertTrue(
//                    result.getExecutionPlan().get().getSteps().stream()
//                            .anyMatch(x -> x instanceof FetchFromIndexStep));
//        }
//    }
//
//    @Test
//    public void testIndexChain() {
//        String className1 = "testIndexChain1";
//        String className2 = "testIndexChain2";
//        String className3 = "testIndexChain3";
//
//        OClass clazz3 = database.createClassIfNotExist(className3);
//        OProperty prop = clazz3.createProperty("name", OType.STRING);
//        prop.createIndex(OClass.INDEX_TYPE.NOTUNIQUE);
//
//        OClass clazz2 = database.createClassIfNotExist(className2);
//        prop = clazz2.createProperty("next", OType.LINK, clazz3);
//        prop.createIndex(OClass.INDEX_TYPE.NOTUNIQUE);
//
//        OClass clazz1 = database.createClassIfNotExist(className1);
//        prop = clazz1.createProperty("next", OType.LINK, clazz2);
//        prop.createIndex(OClass.INDEX_TYPE.NOTUNIQUE);
//
//        OElement elem3 = database.newElement(className3);
//        elem3.setProperty("name", "John");
//        elem3.save();
//
//        OElement elem2 = database.newElement(className2);
//        elem2.setProperty("next", elem3);
//        elem2.save();
//
//        OElement elem1 = database.newElement(className1);
//        elem1.setProperty("next", elem2);
//        elem1.setProperty("name", "right");
//        elem1.save();
//
//        elem1 = database.newElement(className1);
//        elem1.setProperty("name", "wrong");
//        elem1.save();
//
//        try (ResultSet result =
//                     database.query("sql", "select from " + className1 + " where next.next.name = ?", "John")) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertEquals("right", item.getProperty("name"));
//            Assertions.assertFalse(result.hasNext());
//            Assertions.assertTrue(
//                    result.getExecutionPlan().get().getSteps().stream()
//                            .anyMatch(x -> x instanceof FetchFromIndexStep));
//        }
//    }
//
//    @Test
//    public void testQueryView() throws InterruptedException {
//        String className = "testQueryView_Class";
//        String viewName = "testQueryView_View";
//        database.createDocumentType(className);
//        for (int i = 0; i < 10; i++) {
//            OElement elem = database.newElement(className);
//            elem.setProperty("counter", i);
//            elem.save();
//        }
//
//        OViewConfig cfg = new OViewConfig(viewName, "SELECT FROM " + className);
//        final CountDownLatch latch = new CountDownLatch(1);
//        database.getMetadata()
//                .getSchema()
//                .createView(
//                        cfg,
//                        new ViewCreationListener() {
//
//                            @Override
//                            public void afterCreate(ODatabaseSession database, String viewName) {
//                                latch.countDown();
//                            }
//
//                            @Override
//                            public void onError(String viewName, Exception exception) {
//                                latch.countDown();
//                            }
//                        });
//
//        latch.await();
//
//        ResultSet result = database.query("sql", "select FROM " + viewName);
//        int count =
//                result.stream().map(x -> (Integer) x.getProperty("counter")).reduce((x, y) -> x + y).get();
//        Assertions.assertEquals(45, count);
//        result.close();
//    }
//
//    @Test
//    public void testMapByKeyIndex() {
//        String className = "testMapByKeyIndex";
//
//        OClass clazz1 = database.createClassIfNotExist(className);
//        OProperty prop = clazz1.createProperty("themap", OType.EMBEDDEDMAP);
//
//        database.command(
//                "CREATE INDEX " + className + ".themap ON " + className + "(themap by key) NOTUNIQUE");
//
//        for (int i = 0; i < 100; i++) {
//            Map<String, Object> theMap = new HashMap<>();
//            theMap.put("key" + i, "val" + i);
//            OElement elem1 = database.newElement(className);
//            elem1.setProperty("themap", theMap);
//            elem1.save();
//        }
//
//        try (ResultSet result =
//                     database.query("sql", "select from " + className + " where themap CONTAINSKEY ?", "key10")) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Map<String, Object> map = item.getProperty("themap");
//            Assertions.assertEquals("key10", map.keySet().iterator().next());
//            Assertions.assertFalse(result.hasNext());
//            Assertions.assertTrue(
//                    result.getExecutionPlan().get().getSteps().stream()
//                            .anyMatch(x -> x instanceof FetchFromIndexStep));
//        }
//    }
//
//    @Test
//    public void testMapByKeyIndexMultiple() {
//        String className = "testMapByKeyIndexMultiple";
//
//        OClass clazz1 = database.createClassIfNotExist(className);
//        clazz1.createProperty("themap", OType.EMBEDDEDMAP);
//        clazz1.createProperty("thestring", OType.STRING);
//
//        database.command(
//                "CREATE INDEX "
//                        + className
//                        + ".themap_thestring ON "
//                        + className
//                        + "(themap by key, thestring) NOTUNIQUE");
//
//        for (int i = 0; i < 100; i++) {
//            Map<String, Object> theMap = new HashMap<>();
//            theMap.put("key" + i, "val" + i);
//            OElement elem1 = database.newElement(className);
//            elem1.setProperty("themap", theMap);
//            elem1.setProperty("thestring", "thestring" + i);
//            elem1.save();
//        }
//
//        try (ResultSet result =
//                     database.query(
//                             "select from " + className + " where themap CONTAINSKEY ? AND thestring = ?",
//                             "key10",
//                             "thestring10")) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Map<String, Object> map = item.getProperty("themap");
//            Assertions.assertEquals("key10", map.keySet().iterator().next());
//            Assertions.assertFalse(result.hasNext());
//            Assertions.assertTrue(
//                    result.getExecutionPlan().get().getSteps().stream()
//                            .anyMatch(x -> x instanceof FetchFromIndexStep));
//        }
//    }
//
//    @Test
//    public void testMapByValueIndex() {
//        String className = "testMapByValueIndex";
//
//        OClass clazz1 = database.createClassIfNotExist(className);
//        OProperty prop = clazz1.createProperty("themap", OType.EMBEDDEDMAP, OType.STRING);
//
//        database.command(
//                "CREATE INDEX " + className + ".themap ON " + className + "(themap by value) NOTUNIQUE");
//
//        for (int i = 0; i < 100; i++) {
//            Map<String, Object> theMap = new HashMap<>();
//            theMap.put("key" + i, "val" + i);
//            OElement elem1 = database.newElement(className);
//            elem1.setProperty("themap", theMap);
//            elem1.save();
//        }
//
//        try (ResultSet result =
//                     database.query("sql", "select from " + className + " where themap CONTAINSVALUE ?", "val10")) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Map<String, Object> map = item.getProperty("themap");
//            Assertions.assertEquals("key10", map.keySet().iterator().next());
//            Assertions.assertFalse(result.hasNext());
//            Assertions.assertTrue(
//                    result.getExecutionPlan().get().getSteps().stream()
//                            .anyMatch(x -> x instanceof FetchFromIndexStep));
//        }
//    }
//
//    @Test
//    public void testListOfMapsContains() {
//        String className = "testListOfMapsContains";
//
//        OClass clazz1 = database.createClassIfNotExist(className);
//        OProperty prop = clazz1.createProperty("thelist", OType.EMBEDDEDLIST, OType.EMBEDDEDMAP);
//
//        database.command("sql", "insert INTO " + className + " SET thelist = [{name:\"Jack\"}]").close();
//        database.command("sql", "insert INTO " + className + " SET thelist = [{name:\"Joe\"}]").close();
//
//        try (ResultSet result =
//                     database.query("sql", "select from " + className + " where thelist CONTAINS ( name = ?)", "Jack")) {
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertFalse(result.hasNext());
//        }
//    }
//
//    @Test
//    public void testOrderByWithCollate() {
//        String className = "testOrderByWithCollate";
//
//        OClass clazz1 = database.createClassIfNotExist(className);
//
//        database.command("sql", "insert INTO " + className + " SET name = 'A', idx = 0").close();
//        database.command("sql", "insert INTO " + className + " SET name = 'C', idx = 2").close();
//        database.command("sql", "insert INTO " + className + " SET name = 'E', idx = 4").close();
//        database.command("sql", "insert INTO " + className + " SET name = 'b', idx = 1").close();
//        database.command("sql", "insert INTO " + className + " SET name = 'd', idx = 3").close();
//
//        try (ResultSet result =
//                     database.query("sql", "select from " + className + " order by name asc collate ci")) {
//            for (int i = 0; i < 5; i++) {
//                Assertions.assertTrue(result.hasNext());
//                Result item = result.next();
//                int val = item.getProperty("idx");
//                Assertions.assertEquals(i, val);
//            }
//            Assertions.assertFalse(result.hasNext());
//        }
//    }
//
//    @Test
//    public void testContainsEmptyCollection() {
//        String className = "testContainsEmptyCollection";
//
//        database.createClassIfNotExist(className);
//
//        database.command("sql", "insert INTO " + className + " content {\"name\": \"jack\", \"age\": 22}").close();
//        database.command(
//                        "INSERT INTO "
//                                + className
//                                + " content {\"name\": \"rose\", \"age\": 22, \"test\": [[]]}")
//                .close();
//        database.command(
//                        "INSERT INTO "
//                                + className
//                                + " content {\"name\": \"rose\", \"age\": 22, \"test\": [[1]]}")
//                .close();
//        database.command(
//                        "INSERT INTO "
//                                + className
//                                + " content {\"name\": \"pete\", \"age\": 22, \"test\": [{}]}")
//                .close();
//        database.command(
//                        "INSERT INTO "
//                                + className
//                                + " content {\"name\": \"david\", \"age\": 22, \"test\": [\"hello\"]}")
//                .close();
//
//        try (ResultSet result = database.query("sql", "select from " + className + " where test contains []")) {
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertFalse(result.hasNext());
//        }
//    }
//
//    @Test
//    public void testContainsCollection() {
//        String className = "testContainsCollection";
//
//        database.createClassIfNotExist(className);
//
//        database.command("sql", "insert INTO " + className + " content {\"name\": \"jack\", \"age\": 22}").close();
//        database.command(
//                        "INSERT INTO "
//                                + className
//                                + " content {\"name\": \"rose\", \"age\": 22, \"test\": [[]]}")
//                .close();
//        database.command(
//                        "INSERT INTO "
//                                + className
//                                + " content {\"name\": \"rose\", \"age\": 22, \"test\": [[1]]}")
//                .close();
//        database.command(
//                        "INSERT INTO "
//                                + className
//                                + " content {\"name\": \"pete\", \"age\": 22, \"test\": [{}]}")
//                .close();
//        database.command(
//                        "INSERT INTO "
//                                + className
//                                + " content {\"name\": \"david\", \"age\": 22, \"test\": [\"hello\"]}")
//                .close();
//
//        try (ResultSet result = database.query("sql", "select from " + className + " where test contains [1]")) {
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertFalse(result.hasNext());
//        }
//    }
//
//    @Test
//    public void testHeapLimitForOrderBy() {
//        Long oldValue = OGlobalConfiguration.QUERY_MAX_HEAP_ELEMENTS_ALLOWED_PER_OP.getValueAsLong();
//        try {
//            OGlobalConfiguration.QUERY_MAX_HEAP_ELEMENTS_ALLOWED_PER_OP.setValue(3);
//
//            String className = "testHeapLimitForOrderBy";
//
//            database.createClassIfNotExist(className);
//
//            database.command("sql", "insert INTO " + className + " set name = 'a'").close();
//            database.command("sql", "insert INTO " + className + " set name = 'b'").close();
//            database.command("sql", "insert INTO " + className + " set name = 'c'").close();
//            database.command("sql", "insert INTO " + className + " set name = 'd'").close();
//
//            try {
//                try (ResultSet result = database.query("sql", "select from " + className + " ORDER BY name")) {
//                    result.forEachRemaining(x -> x.getProperty("name"));
//                }
//                Assertions.fail();
//            } catch (OCommandExecutionException ex) {
//            }
//        } finally {
//            OGlobalConfiguration.QUERY_MAX_HEAP_ELEMENTS_ALLOWED_PER_OP.setValue(oldValue);
//        }
//    }
//
//    @Test
//    public void testXor() {
//        try (ResultSet result = database.query("sql", "select 15 ^ 4 as foo")) {
//            Assertions.assertTrue(result.hasNext());
//            Result item = result.next();
//            Assertions.assertEquals(11, (int) item.getProperty("foo"));
//            Assertions.assertFalse(result.hasNext());
//        }
//    }
//
//    @Test
//    public void testLike() {
//        String className = "testLike";
//
//        database.createClassIfNotExist(className);
//
//        database.command("sql", "insert INTO " + className + " content {\"name\": \"foobarbaz\"}").close();
//        database.command("sql", "insert INTO " + className + " content {\"name\": \"test[]{}()|*^.test\"}").close();
//
//        try (ResultSet result = database.query("sql", "select from " + className + " where name LIKE 'foo%'")) {
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertFalse(result.hasNext());
//        }
//        try (ResultSet result =
//                     database.query("sql", "select from " + className + " where name LIKE '%foo%baz%'")) {
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertFalse(result.hasNext());
//        }
//        try (ResultSet result = database.query("sql", "select from " + className + " where name LIKE '%bar%'")) {
//            Assertions.assertTrue(result.hasNext());
//            result.next();
//            Assertions.assertFalse(result.hasNext());
//        }
//
//        try (ResultSet result = database.query("sql", "select from " + className + " where name LIKE 'bar%'")) {
//            Assertions.assertFalse(result.hasNext());
//        }
//
//        try (ResultSet result = database.query("sql", "select from " + className + " where name LIKE '%bar'")) {
//            Assertions.assertFalse(result.hasNext());
//        }
//
//        String specialChars = "[]{}()|*^.";
//        for (char c : specialChars.toCharArray()) {
//            try (ResultSet result =
//                         database.query("sql", "select from " + className + " where name LIKE '%" + c + "%'")) {
//                Assertions.assertTrue(result.hasNext());
//                result.next();
//                Assertions.assertFalse(result.hasNext());
//            }
//        }
//    }
//
//    @Test
//    public void testCountGroupBy() {
//        // issue #9288
//        String className = "testCountGroupBy";
//        database.getSchema().createDocumentType(className);
//        for (int i = 0; i < 10; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("type", i % 2 == 0 ? "even" : "odd");
//            doc.set("val", i);
//            doc.save();
//        }
//        ResultSet result = database.query("sql", "select count(val) as count from " + className + " limit 3");
//        printExecutionPlan(result);
//
//        Assertions.assertTrue(result.hasNext());
//        Result item = result.next();
//        Assertions.assertEquals(10L, (long) item.getProperty("count"));
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testTimeout() {
//        String className = "testTimeout";
//        final String funcitonName = getClass().getSimpleName() + "_sleep";
//        database.getSchema().createDocumentType(className);
//
//        OSQLEngine.getInstance()
//                .registerFunction(
//                        funcitonName,
//                        new OSQLFunction() {
//
//                            @Override
//                            public Object execute(
//                                    Object iThis,
//                                    OIdentifiable iCurrentRecord,
//                                    Object iCurrentResult,
//                                    Object[] iParams,
//                                    OCommandContext iContext) {
//                                try {
//                                    Thread.sleep(5);
//                                } catch (InterruptedException e) {
//                                }
//                                return null;
//                            }
//
//                            @Override
//                            public void config(Object[] configuredParameters) {}
//
//                            @Override
//                            public boolean aggregateResults() {
//                                return false;
//                            }
//
//                            @Override
//                            public boolean filterResult() {
//                                return false;
//                            }
//
//                            @Override
//                            public String getName() {
//                                return funcitonName;
//                            }
//
//                            @Override
//                            public int getMinParams() {
//                                return 0;
//                            }
//
//                            @Override
//                            public int getMaxParams() {
//                                return 0;
//                            }
//
//                            @Override
//                            public String getSyntax() {
//                                return "";
//                            }
//
//                            @Override
//                            public Object getResult() {
//                                return null;
//                            }
//
//                            @Override
//                            public void setResult(Object iResult) {}
//
//                            @Override
//                            public boolean shouldMergeDistributedResult() {
//                                return false;
//                            }
//
//                            @Override
//                            public Object mergeDistributedResult(List<Object> resultsToMerge) {
//                                return null;
//                            }
//                        });
//        for (int i = 0; i < 3; i++) {
//            MutableDocument doc = database.newDocument(className);
//            doc.set("type", i % 2 == 0 ? "even" : "odd");
//            doc.set("val", i);
//            doc.save();
//        }
//        try (ResultSet result =
//                     database.query("sql", "select " + funcitonName + "(), * from " + className + " timeout 1")) {
//            while (result.hasNext()) {
//                result.next();
//            }
//            Assertions.fail();
//        } catch (OTimeoutException ex) {
//
//        }
//
//        try (ResultSet result =
//                     database.query("sql", "select " + funcitonName + "(), * from " + className + " timeout 100")) {
//            while (result.hasNext()) {
//                result.next();
//            }
//        } catch (OTimeoutException ex) {
//            Assertions.fail();
//        }
//    }
//
//    @Test
//    public void testSimpleRangeQueryWithIndexGTE() {
//        final String className = "testSimpleRangeQueryWithIndexGTE";
//        final OClass clazz = database.getSchema().getOrcreateDocumentType(className);
//        final OProperty prop = clazz.createProperty("name", OType.STRING);
//        prop.createIndex(OClass.INDEX_TYPE.NOTUNIQUE);
//
//        for (int i = 0; i < 10; i++) {
//            final MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.save();
//        }
//        final ResultSet result = database.query("sql", "select from " + className + " WHERE name >= 'name5'");
//        printExecutionPlan(result);
//
//        for (int i = 0; i < 5; i++) {
//            Assertions.assertTrue(result.hasNext());
//            System.out.println(result.next());
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//    @Test
//    public void testSimpleRangeQueryWithIndexLTE() {
//        final String className = "testSimpleRangeQueryWithIndexLTE";
//        final OClass clazz = database.getSchema().getOrcreateDocumentType(className);
//        final OProperty prop = clazz.createProperty("name", OType.STRING);
//        prop.createIndex(OClass.INDEX_TYPE.NOTUNIQUE);
//
//        for (int i = 0; i < 10; i++) {
//            final MutableDocument doc = database.newDocument(className);
//            doc.set("name", "name" + i);
//            doc.save();
//        }
//        final ResultSet result = database.query("sql", "select from " + className + " WHERE name <= 'name5'");
//        printExecutionPlan(result);
//
//        for (int i = 0; i < 6; i++) {
//            Assertions.assertTrue(result.hasNext());
//            System.out.println(result.next());
//        }
//        Assertions.assertFalse(result.hasNext());
//        result.close();
//    }
//
//
//
//    @Test
//    public void testComplexIndexChain() {
//
//        // A -b-> B -c-> C -d-> D.name
//        //               C.name
//
//        String classNamePrefix = "testComplexIndexChain_";
//        DocumentType a = database.getSchema().createDocumentType(classNamePrefix + "A");
//        DocumentType b = database.getSchema().createDocumentType(classNamePrefix + "C");
//        DocumentType c = database.getSchema().createDocumentType(classNamePrefix + "B");
//        DocumentType d = database.getSchema().createDocumentType(classNamePrefix + "D");
//
//        a.createProperty("b", Type.LINK).createIndex(Schema.INDEX_TYPE.LSM_TREE, false);
//        b.createProperty("c", Type.LINK).createIndex(Schema.INDEX_TYPE.LSM_TREE, false);
//        c.createProperty("d", Type.LINK).createIndex(Schema.INDEX_TYPE.LSM_TREE, false);
//        c.createProperty("name", Type.STRING).createIndex(Schema.INDEX_TYPE.LSM_TREE, false);
//        d.createProperty("name", Type.STRING).createIndex(Schema.INDEX_TYPE.LSM_TREE, false);
//
//        MutableDocument dDoc = database.newDocument(d.getName());
//        dDoc.set("name", "foo");
//        dDoc.save();
//
//        MutableDocument cDoc = database.newDocument(c.getName());
//        cDoc.set("name", "foo");
//        cDoc.set("d", dDoc);
//        cDoc.save();
//
//        MutableDocument bDoc = database.newDocument(b.getName());
//        bDoc.set("c", cDoc);
//        bDoc.save();
//
//        MutableDocument aDoc = database.newDocument(a.getName());
//        aDoc.set("b", bDoc);
//        aDoc.save();
//
//        try (ResultSet rs =
//                     database.query(
//                             "SELECT FROM "
//                                     + classNamePrefix
//                                     + "A WHERE b.c.name IN ['foo'] AND b.c.d.name IN ['foo']")) {
//            Assertions.assertTrue(rs.hasNext());
//        }
//
//        try (ResultSet rs =
//                     database.query(
//                             "SELECT FROM " + classNamePrefix + "A WHERE b.c.name = 'foo' AND b.c.d.name = 'foo'")) {
//            Assertions.assertTrue(rs.hasNext());
//            Assertions.assertTrue(
//                    rs.getExecutionPlan().get().getSteps().stream()
//                            .anyMatch(x -> x instanceof FetchFromIndexStep));
//        }
//    }
//
//    @Test
//    public void testIndexWithSubquery() {
//        String classNamePrefix = "testIndexWithSubquery_";
//        database.command("create class " + classNamePrefix + "Ownership extends V abstract;").close();
//        database.command("create class " + classNamePrefix + "User extends V;").close();
//        database.command("create property " + classNamePrefix + "User.id String;").close();
//        database.command(
//                        "create index "
//                                + classNamePrefix
//                                + "User.id ON "
//                                + classNamePrefix
//                                + "User(id) unique;")
//                .close();
//        database.command(
//                        "create class " + classNamePrefix + "Report extends " + classNamePrefix + "Ownership;")
//                .close();
//        database.command("create property " + classNamePrefix + "Report.id String;").close();
//        database.command("create property " + classNamePrefix + "Report.label String;").close();
//        database.command("create property " + classNamePrefix + "Report.format String;").close();
//        database.command("create property " + classNamePrefix + "Report.source String;").close();
//        database.command("create class " + classNamePrefix + "hasOwnership extends E;").close();
//        database.command("sql", "insert into " + classNamePrefix + "User content {id:\"admin\"};");
//        database.command(
//                        "insert into "
//                                + classNamePrefix
//                                + "Report content {format:\"PDF\", id:\"rep1\", label:\"Report 1\", source:\"Report1.src\"};")
//                .close();
//        database.command(
//                        "insert into "
//                                + classNamePrefix
//                                + "Report content {format:\"CSV\", id:\"rep2\", label:\"Report 2\", source:\"Report2.src\"};")
//                .close();
//        database.command(
//                        "create edge "
//                                + classNamePrefix
//                                + "hasOwnership from (select from "
//                                + classNamePrefix
//                                + "User) to (select from "
//                                + classNamePrefix
//                                + "Report);")
//                .close();
//
//        try (ResultSet rs =
//                     database.query(
//                             "select from "
//                                     + classNamePrefix
//                                     + "Report where id in (select out('"
//                                     + classNamePrefix
//                                     + "hasOwnership').id from "
//                                     + classNamePrefix
//                                     + "User where id = 'admin');")) {
//            Assertions.assertTrue(rs.hasNext());
//            rs.next();
//            Assertions.assertTrue(rs.hasNext());
//            rs.next();
//            Assertions.assertFalse(rs.hasNext());
//        }
//
//        database.command(
//                        "create index "
//                                + classNamePrefix
//                                + "Report.id ON "
//                                + classNamePrefix
//                                + "Report(id) unique;")
//                .close();
//
//        try (ResultSet rs =
//                     database.query(
//                             "select from "
//                                     + classNamePrefix
//                                     + "Report where id in (select out('"
//                                     + classNamePrefix
//                                     + "hasOwnership').id from "
//                                     + classNamePrefix
//                                     + "User where id = 'admin');")) {
//            Assertions.assertTrue(rs.hasNext());
//            rs.next();
//            Assertions.assertTrue(rs.hasNext());
//            rs.next();
//            Assertions.assertFalse(rs.hasNext());
//        }
//    }
//
//    @Test
//    public void testExclude() {
//        String className = "TestExclude";
//        database.getSchema().createDocumentType(className);
//        MutableDocument doc = database.newDocument(className);
//        doc.set("name", "foo");
//        doc.set("surname", "bar");
//        doc.save();
//
//        ResultSet result = database.query("sql", "select *, !surname from " + className);
//        Assertions.assertTrue(result.hasNext());
//        Result item = result.next();
//        Assertions.assertNotNull(item);
//        Assertions.assertEquals("foo", item.getProperty("name"));
//        Assertions.assertNull(item.getProperty("surname"));
//
//        printExecutionPlan(result);
//        result.close();
//    }

    public static void printExecutionPlan(ResultSet result) {
        printExecutionPlan(null, result);
    }

    public static void printExecutionPlan(String query, ResultSet result) {
//        if (query != null) {
//            System.out.println(query);
//        }
//        result.getExecutionPlan().ifPresent(x -> System.out.println(x.prettyPrint(0, 3)));
//        System.out.println();
    }
}