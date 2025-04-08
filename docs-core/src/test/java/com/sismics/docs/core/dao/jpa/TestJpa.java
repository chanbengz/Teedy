package com.sismics.docs.core.dao.jpa;

import com.sismics.docs.BaseTransactionalTest;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.TransactionUtil;
import com.sismics.docs.core.util.authentication.InternalAuthenticationHandler;
import org.junit.Assert;
import org.junit.Test;
import com.sismics.docs.core.dao.GroupDao;
import com.sismics.docs.core.dao.criteria.GroupCriteria;
import com.sismics.docs.core.dao.dto.GroupDto;
import com.sismics.docs.core.model.jpa.Group;
import com.sismics.docs.core.model.jpa.UserGroup;
import java.util.List;

/**
 * Tests the persistance layer.
 * 
 * @author jtremeaux
 */
public class TestJpa extends BaseTransactionalTest {
    @Test
    public void testJpa() throws Exception {
        // Create a user
        UserDao userDao = new UserDao();
        User user = createUser("testJpa");

        TransactionUtil.commit();

        // Search a user by his ID
        user = userDao.getById(user.getId());
        Assert.assertNotNull(user);
        Assert.assertEquals("toto@docs.com", user.getEmail());

        // Authenticate using the database
        Assert.assertNotNull(new InternalAuthenticationHandler().authenticate("testJpa", "12345678"));

        // Delete the created user
        userDao.delete("testJpa", user.getId());
        TransactionUtil.commit();
    }

    @Test
    public void testJpa2() throws Exception {
        UserDao userDao = new UserDao();
        User user = createUser("groupTest");
        TransactionUtil.commit();

        GroupDao groupDao = new GroupDao();
        Group group = new Group();
        group.setName("testGroup");
        String groupId = groupDao.create(group, user.getId());
        TransactionUtil.commit();

        // Get the group by ID
        Group groupById = groupDao.getActiveById(groupId);
        Assert.assertNotNull(groupById);
        Assert.assertEquals("testGroup", groupById.getName());

        Group groupByName = groupDao.getActiveByName("testGroup");
        Assert.assertNotNull(groupByName);
        Assert.assertEquals(groupId, groupByName.getId());

        Group childGroup = new Group();
        childGroup.setName("childGroup");
        childGroup.setParentId(groupId);
        String childGroupId = groupDao.create(childGroup, user.getId());
        TransactionUtil.commit();

        group.setName("updatedGroup");
        groupDao.update(group, user.getId());
        TransactionUtil.commit();

        Group updatedGroup = groupDao.getActiveById(groupId);
        Assert.assertEquals("updatedGroup", updatedGroup.getName());

        UserGroup userGroup = new UserGroup();
        userGroup.setGroupId(groupId);
        userGroup.setUserId(user.getId());
        String userGroupId = groupDao.addMember(userGroup);
        TransactionUtil.commit();

        GroupCriteria criteria = new GroupCriteria();
        criteria.setSearch("updated");
        List<GroupDto> groups = groupDao.findByCriteria(criteria, null);
        Assert.assertFalse(groups.isEmpty());
        Assert.assertEquals(groupId, groups.get(0).getId());

        criteria = new GroupCriteria();
        criteria.setUserId(user.getId());
        criteria.setRecursive(true);
        groups = groupDao.findByCriteria(criteria, null);
        Assert.assertFalse(groups.isEmpty());

        groupDao.removeMember(groupId, user.getId());
        TransactionUtil.commit();

        groupDao.delete(childGroupId, user.getId());
        groupDao.delete(groupId, user.getId());
        TransactionUtil.commit();

        Assert.assertNull(groupDao.getActiveById(groupId));
        Assert.assertNull(groupDao.getActiveById(childGroupId));

        userDao.delete("groupTest", user.getId());
        TransactionUtil.commit();
    }
}
