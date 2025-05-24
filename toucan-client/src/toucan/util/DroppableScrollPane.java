package toucan.util;

import toucan.gui.*;
import java.awt.*;
import java.awt.dnd.*;
import javax.swing.*;
import java.awt.datatransfer.*;
import java.util.*;
import java.io.*;

public class DroppableScrollPane extends JScrollPane
    implements DropTargetListener, DragSourceListener, DragGestureListener
{
    DropTarget dropTarget = new DropTarget (this, this);
    DragSource dragSource = DragSource.getDefaultDragSource();
    public String fileName;
    public MainFrame owner;

    public DroppableScrollPane(MainFrame owner,JLabel img)
    {
     super(img);
      this.owner = owner;
      dragSource.createDefaultDragGestureRecognizer(
          this, DnDConstants.ACTION_COPY_OR_MOVE, this);
    }

    public void dragDropEnd(DragSourceDropEvent DragSourceDropEvent){}
    public void dragEnter(DragSourceDragEvent DragSourceDragEvent){}
    public void dragExit(DragSourceEvent DragSourceEvent){}
    public void dragOver(DragSourceDragEvent DragSourceDragEvent){}
    public void dropActionChanged(DragSourceDragEvent DragSourceDragEvent){}

    public void dragEnter (DropTargetDragEvent dropTargetDragEvent)
    {
      dropTargetDragEvent.acceptDrag (DnDConstants.ACTION_COPY_OR_MOVE);
    }

    public void dragExit (DropTargetEvent dropTargetEvent) {}
    public void dragOver (DropTargetDragEvent dropTargetDragEvent) {}
    public void dropActionChanged (DropTargetDragEvent dropTargetDragEvent){}

    public void drop (DropTargetDropEvent dropTargetDropEvent)
    {
        try
        {
            Transferable tr = dropTargetDropEvent.getTransferable();
            if (tr.isDataFlavorSupported (DataFlavor.javaFileListFlavor))
            {
                dropTargetDropEvent.acceptDrop (
                    DnDConstants.ACTION_COPY_OR_MOVE);
                java.util.List fileList = (java.util.List)
                    tr.getTransferData(DataFlavor.javaFileListFlavor);
                Iterator iterator = fileList.iterator();
                while (iterator.hasNext())
                {
                  File file = (File)iterator.next();
                  try{
                    if(file.getName().endsWith(".gff")){
                      owner.gl.addGffFromFile(file.getAbsolutePath());
                      owner.setImg();
                    }
                    else{
                      owner.setGeneList(file.getAbsolutePath());
                      System.out.println("dropped " + file.getAbsolutePath());
                      owner.setImg();
                    }
                  }
                  catch(Exception ex){
                    ex.printStackTrace();
                  }
                }
                dropTargetDropEvent.getDropTargetContext().dropComplete(true);
          } else {
            System.err.println ("Rejected");
            dropTargetDropEvent.rejectDrop();
          }
        } catch (IOException io) {
            io.printStackTrace();
            dropTargetDropEvent.rejectDrop();
        } catch (UnsupportedFlavorException ufe) {
            ufe.printStackTrace();
            dropTargetDropEvent.rejectDrop();
        }
    }

    public void dragGestureRecognized(DragGestureEvent dragGestureEvent)
    {
            //FileSelection transferable =
            //  new FileSelection(new File("test.embl"));
            //dragGestureEvent.startDrag(
            //  DragSource.DefaultCopyDrop,
            //  transferable,
            //  this);

    }


    public class FileSelection extends Vector implements Transferable
    {
        final static int FILE = 0;
        final static int STRING = 1;
        DataFlavor flavors[] = {DataFlavor.javaFileListFlavor,
                                DataFlavor.stringFlavor};
        public FileSelection(File file)
        {
            addElement(file);
        }
        /* Returns the array of flavors in which it can provide the data. */
        public synchronized DataFlavor[] getTransferDataFlavors() {
        return flavors;
        }
        /* Returns whether the requested flavor is supported by this object. */
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            boolean b  = false;
            b |=flavor.equals(flavors[FILE]);
            b |= flavor.equals(flavors[STRING]);
                return (b);
        }
        /**
         * If the data was requested in the "java.lang.String" flavor,
         * return the String representing the selection.
         */
        public synchronized Object getTransferData(DataFlavor flavor)
                        throws UnsupportedFlavorException, IOException {
        if (flavor.equals(flavors[FILE])) {
            return this;
        } else if (flavor.equals(flavors[STRING])) {
            return((File)elementAt(0)).getAbsolutePath();
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
        }
    }


    }
