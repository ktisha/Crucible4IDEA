package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.changes.ui.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.util.*;

/**
 * User : ktisha
 */
public class TreeModelBuilder {
  @NonNls public static final String ROOT_NODE_VALUE = "root";

  private final Project myProject;
  private final boolean showFlatten;
  private DefaultTreeModel myModel;
  private final ChangesBrowserNode root;
  private boolean myPolicyInitialized;
  private ChangesGroupingPolicy myPolicy;
  private HashMap<String, ChangesBrowserNode> myFoldersCache;

  public TreeModelBuilder(final Project project, final boolean showFlatten) {
    myProject = project;
    this.showFlatten = showFlatten;
    root = ChangesBrowserNode.create(myProject, ROOT_NODE_VALUE);
    myModel = new DefaultTreeModel(root);
    myFoldersCache = new HashMap<String, ChangesBrowserNode>();
  }

  @Nullable
  private ChangesGroupingPolicy createGroupingPolicy() {
    if (! myPolicyInitialized) {
      myPolicyInitialized = true;
      final ChangesGroupingPolicyFactory factory = ChangesGroupingPolicyFactory.getInstance(myProject);
      if (factory != null) {
        myPolicy = factory.createGroupingPolicy(myModel);
      }
    }
    return myPolicy;
  }

  private static class MyChangeNodeUnderChangeListDecorator extends RemoteStatusChangeNodeDecorator {
    private final ChangeListRemoteState.Reporter myReporter;

    private MyChangeNodeUnderChangeListDecorator(final RemoteRevisionsCache remoteRevisionsCache, final ChangeListRemoteState.Reporter reporter) {
      super(remoteRevisionsCache);
      myReporter = reporter;
    }

    @Override
    protected void reportState(boolean state) {
      myReporter.report(state);
    }

    @Override
    public void preDecorate(Change change, ChangesBrowserNodeRenderer renderer, boolean showFlatten) {
    }
  }

  private void resetGrouping() {
    myFoldersCache = new HashMap<String, ChangesBrowserNode>();
    myPolicyInitialized = false;
  }

  public DefaultTreeModel buildModel(@NotNull List<? extends ChangeList> changeLists) {
    final RemoteRevisionsCache revisionsCache = RemoteRevisionsCache.getInstance(myProject);
    for (ChangeList list : changeLists) {
      final List<Change> changes = new ArrayList<Change>(list.getChanges());
      final ChangeListRemoteState listRemoteState = new ChangeListRemoteState(changes.size());
      ChangesBrowserNode listNode = new ChangesBrowserChangeListNode(myProject, list, listRemoteState);
      myModel.insertNodeInto(listNode, root, 0);
      resetGrouping();
      final ChangesGroupingPolicy policy = createGroupingPolicy();
      int i = 0;
      Collections.sort(changes, MyChangePathLengthComparator.getInstance());
      for (final Change change : changes) {
        final MyChangeNodeUnderChangeListDecorator decorator =
          new MyChangeNodeUnderChangeListDecorator(revisionsCache, new ChangeListRemoteState.Reporter(i, listRemoteState));
        insertChangeNode(change, policy, listNode, new Computable<ChangesBrowserNode>() {
          @Override
          public ChangesBrowserNode compute() {
            return new ChangesBrowserChangeNode(myProject, change, decorator);
          }
        });
        ++ i;
      }
    }
    return myModel;
  }

  private static class MyChangePathLengthComparator implements Comparator<Change> {
    private final static MyChangePathLengthComparator ourInstance = new MyChangePathLengthComparator();

    public static MyChangePathLengthComparator getInstance() {
      return ourInstance;
    }

    @Override
    public int compare(Change o1, Change o2) {
      final FilePath fp1 = ChangesUtil.getFilePath(o1);
      final FilePath fp2 = ChangesUtil.getFilePath(o2);

      final int diff = fp1.getIOFile().getPath().length() - fp2.getIOFile().getPath().length();
      return diff == 0 ? 0 : diff < 0 ? -1 : 1;
    }
  }

  private void insertChangeNode(final Object change, final ChangesGroupingPolicy policy,
                                final ChangesBrowserNode listNode, final Computable<ChangesBrowserNode> nodeCreator) {
    final StaticFilePath pathKey = getKey(change);
    final ChangesBrowserNode node = nodeCreator.compute();
    ChangesBrowserNode parentNode = getParentNodeFor(pathKey, policy, listNode);
    myModel.insertNodeInto(node, parentNode, myModel.getChildCount(parentNode));

    if (pathKey != null && pathKey.isDirectory()) {
      myFoldersCache.put(pathKey.getKey(), node);
    }
  }

  private static StaticFilePath getKey(final Object o) {
    if (o instanceof Change) {
      return staticFrom(ChangesUtil.getFilePath((Change) o));
    }
    else if (o instanceof VirtualFile) {
      return staticFrom((VirtualFile) o);
    }
    else if (o instanceof FilePath) {
      return staticFrom((FilePath) o);
    } else if (o instanceof ChangesBrowserLogicallyLockedFile) {
      return staticFrom(((ChangesBrowserLogicallyLockedFile) o).getUserObject());
    } else if (o instanceof LocallyDeletedChange) {
      return staticFrom(((LocallyDeletedChange) o).getPath());
    }

    return null;
  }

  private static StaticFilePath staticFrom(final FilePath fp) {
    final String path = fp.getPath();
    if (fp.isNonLocal() && (! FileUtil.isAbsolute(path) || VcsUtil.isPathRemote(path))) {
      return new StaticFilePath(fp.isDirectory(), fp.getIOFile().getPath().replace('\\', '/'), fp.getVirtualFile());
    }
    return new StaticFilePath(fp.isDirectory(), new File(fp.getIOFile().getPath().replace('\\', '/')).getAbsolutePath(), fp.getVirtualFile());
  }
  
  private static StaticFilePath staticFrom(final VirtualFile vf) {
    return new StaticFilePath(vf.isDirectory(), vf.getPath(), vf);
  }

  private ChangesBrowserNode getParentNodeFor(final StaticFilePath nodePath, @Nullable ChangesGroupingPolicy policy, ChangesBrowserNode rootNode) {
    if (showFlatten) {
      return rootNode;
    }

    if (policy != null) {
      ChangesBrowserNode nodeFromPolicy = policy.getParentNodeFor(nodePath, rootNode);
      if (nodeFromPolicy != null) {
        return nodeFromPolicy;
      }
    }

    final StaticFilePath parentPath = nodePath.getParent();
    if (parentPath == null) {
      return rootNode;
    }

    ChangesBrowserNode parentNode = myFoldersCache.get(parentPath.getKey());
    if (parentNode == null) {
      parentNode = ChangesBrowserNode.create(myProject, new FilePathImpl(new File(parentPath.getPath()), true));
      ChangesBrowserNode grandPa = getParentNodeFor(parentPath, policy, rootNode);
      myModel.insertNodeInto(parentNode, grandPa, grandPa.getChildCount());
      myFoldersCache.put(parentPath.getKey(), parentNode);
    }

    return parentNode;
  }

}
