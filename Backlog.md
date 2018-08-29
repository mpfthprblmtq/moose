# Moose Backlog

---

Problem:
Sometimes the track and disk number is set to /0 when you save it with nothing in the track or disk slot

Test Case:
Save a track with nothing in track or disk and then reopen it

---

~~Problem:~~
~~When editing a field on the table, another row’s icon can change to blue~~

~~Test Case:~~

Fixed by changing the TCL to check if the value actually changed before setting the row icon

```java
if(!tcl.getNewValue().equals(tcl.getOldValue())) {
  setTitle(index, tcl.getNewValue().toString());
  setRowIcon(EDITED, tcl.getRow());
  songEdited(index);
} else {
  // do nothing, nothing was changed
}
```

---
