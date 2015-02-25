package com.docdoku.server;

import com.docdoku.core.common.*;
import com.docdoku.core.exceptions.AccessRightException;
import com.docdoku.core.security.ACL;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.workflow.Workflow;
import com.docdoku.core.workflow.WorkflowModel;
import com.docdoku.core.workflow.WorkflowModelKey;
import com.docdoku.server.util.WorkflowUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.persistence.EntityManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class WorkflowManagerBeanTest {


    @InjectMocks
    WorkflowManagerBean workflowManagerBean = new WorkflowManagerBean();

    @Mock
    private EntityManager em;
    @Mock
    private IUserManagerLocal userManager;

    private User user;
    private Account account;
    private Workspace workspace;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        account = new Account(WorkflowUtil.ADMIN_LOGIN, WorkflowUtil.ADMIN_NAME, WorkflowUtil.ADMIN_MAIL, "en", new Date(), null);
        workspace = new Workspace(WorkflowUtil.WORKSPACE_ID, account, WorkflowUtil.WORKSPACE_DESCRIPTION, false);
        user = new User(workspace,WorkflowUtil.USER_LOGIN , WorkflowUtil.USER_NAME,WorkflowUtil.USER_MAIL, "en");
    }

    /**
     * test the remove of acl from a workflow operated by user who doesn't have write access to the workflow
     * @throws Exception
     */
    @Test(expected = AccessRightException.class)
    public void testRemoveACLFromWorkflow() throws Exception {

        //Given
        WorkflowModel workflowModel = new WorkflowModel(workspace, WorkflowUtil.WORKSPACE_ID, user, "");
        ACL acl = new ACL();
        acl.addEntry(user, ACL.Permission.READ_ONLY);
        workflowModel.setAcl(acl);
        // User has read access to the workspace
        Mockito.when(userManager.checkWorkspaceReadAccess(WorkflowUtil.WORKSPACE_ID)).thenReturn(user);
        Mockito.when(em.find(WorkflowModel.class, new WorkflowModelKey(WorkflowUtil.WORKSPACE_ID,WorkflowUtil.WORKFLOW_MODEL_ID))).thenReturn(workflowModel);
        //When
        workflowManagerBean.removeACLFromWorkflow(WorkflowUtil.WORKSPACE_ID, WorkflowUtil.WORKFLOW_MODEL_ID);
        //Then, removeACLFromWorkflow should throw AccessRightException, user doesn't have write access to the workflow
    }

    /**
     * create an ACL of the workflow, user is the admin of the workspace
     * @throws Exception
     */
    @Test
    public void testUpdateACLForWorkflowWithNoACL() throws Exception {

        //Given
        WorkflowModel workflowModel = new WorkflowModel(workspace, WorkflowUtil.WORKSPACE_ID, user, "");
        Map<String, String> userEntries = new HashMap<>();
        User user2 = new User(workspace,WorkflowUtil.USER2_LOGIN , WorkflowUtil.USER2_NAME,WorkflowUtil.USER2_MAIL, "en");
        User user3 = new User(workspace,WorkflowUtil.USER3_LOGIN , WorkflowUtil.USER3_NAME,WorkflowUtil.USER3_MAIL, "en");
        userEntries.put(user.getLogin(), ACL.Permission.FORBIDDEN.name());
        userEntries.put(user2.getLogin(), ACL.Permission.READ_ONLY.name());
        userEntries.put(user3.getLogin(), ACL.Permission.FULL_ACCESS.name());

        // User has read access to the workspace
        Mockito.when(userManager.checkWorkspaceReadAccess(WorkflowUtil.WORKSPACE_ID)).thenReturn(user);
        Mockito.when(em.find(WorkflowModel.class, new WorkflowModelKey(WorkflowUtil.WORKSPACE_ID, WorkflowUtil.WORKFLOW_MODEL_ID))).thenReturn(workflowModel);
        Mockito.when(em.find(User.class, new UserKey(WorkflowUtil.WORKSPACE_ID, WorkflowUtil.USER_LOGIN))).thenReturn(user);
        Mockito.when(em.find(User.class, new UserKey(WorkflowUtil.WORKSPACE_ID,WorkflowUtil.USER2_LOGIN))).thenReturn(user2);
        Mockito.when(em.find(User.class, new UserKey(WorkflowUtil.WORKSPACE_ID,WorkflowUtil.USER3_LOGIN))).thenReturn(user3);

        //When
        WorkflowModel workflow= workflowManagerBean.updateACLForWorkflow(WorkflowUtil.WORKSPACE_ID, WorkflowUtil.WORKFLOW_MODEL_ID, userEntries, null);
        //Then
        Assert.assertEquals(workflow.getAcl().getGroupEntries().size() ,0 );
        Assert.assertEquals(workflow.getAcl().getUserEntries().size() , 3);
        Assert.assertEquals(workflow.getAcl().getUserEntries().get(user).getPermission() , ACL.Permission.FORBIDDEN);
        Assert.assertEquals(workflow.getAcl().getUserEntries().get(user2).getPermission() , ACL.Permission.READ_ONLY);
        Assert.assertEquals(workflow.getAcl().getUserEntries().get(user3).getPermission() , ACL.Permission.FULL_ACCESS);


    }

    @Test
    public void testUpdateACLForWorkflowWithAnExistingACL() throws Exception {
        //Given
        Map<String, String> userEntries = new HashMap<>();
        Map<String, String> grpEntries = new HashMap<>();
        User user2 = new User(workspace,WorkflowUtil.USER2_LOGIN , WorkflowUtil.USER2_NAME,WorkflowUtil.USER2_MAIL, "en");
        User user3 = new User(workspace,WorkflowUtil.USER3_LOGIN , WorkflowUtil.USER3_NAME,WorkflowUtil.USER3_MAIL, "en");
        UserGroup group1 = new UserGroup(workspace,WorkflowUtil.GRP1_ID);

        WorkflowModel workflowModel = new WorkflowModel(workspace, WorkflowUtil.WORKSPACE_ID, user, "");
        ACL acl = new ACL();
        // user2 had READ_ONLY access in the existing acl
        acl.addEntry(user2, ACL.Permission.READ_ONLY);
        acl.addEntry(group1, ACL.Permission.FULL_ACCESS);
        workflowModel.setAcl(acl);

        userEntries.put(user.getLogin(), ACL.Permission.FORBIDDEN.name());
        // user2 has non access FORBIDDEN in the new acl
        userEntries.put(user2.getLogin(), ACL.Permission.FORBIDDEN.name());
        userEntries.put(user3.getLogin(), ACL.Permission.FULL_ACCESS.name());


        //user2 belong to group1
        group1.addUser(user2);
        group1.addUser(user);
        //group1 has FULL_ACCESS
        grpEntries.put(group1.getId(),ACL.Permission.FULL_ACCESS.name());


        // User has read access to the workspace
        Mockito.when(userManager.checkWorkspaceReadAccess(WorkflowUtil.WORKSPACE_ID)).thenReturn(user);
        Mockito.when(em.find(WorkflowModel.class, new WorkflowModelKey(WorkflowUtil.WORKSPACE_ID,WorkflowUtil.WORKFLOW_MODEL_ID))).thenReturn(workflowModel);
        Mockito.when(em.find(User.class, new UserKey(WorkflowUtil.WORKSPACE_ID,WorkflowUtil.USER_LOGIN))).thenReturn(user);
        Mockito.when(em.find(User.class, new UserKey(WorkflowUtil.WORKSPACE_ID,WorkflowUtil.USER2_LOGIN))).thenReturn(user2);
        Mockito.when(em.find(User.class, new UserKey(WorkflowUtil.WORKSPACE_ID,WorkflowUtil.USER3_LOGIN))).thenReturn(user3);

        //When
        WorkflowModel workflow= workflowManagerBean.updateACLForWorkflow(WorkflowUtil.WORKSPACE_ID, WorkflowUtil.WORKFLOW_MODEL_ID, userEntries, grpEntries);
        //Then
        Assert.assertEquals(workflow.getAcl().getGroupEntries().size(),1 );
        Assert.assertEquals(workflow.getAcl().getUserEntries().size() , 3);
        Assert.assertEquals(workflow.getAcl().getUserEntries().get(user).getPermission() , ACL.Permission.FORBIDDEN);
        Assert.assertEquals(workflow.getAcl().getUserEntries().get(user2).getPermission() , ACL.Permission.FORBIDDEN);
        Assert.assertEquals(workflow.getAcl().getUserEntries().get(user3).getPermission() , ACL.Permission.FULL_ACCESS);

    }


}