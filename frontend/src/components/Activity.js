import React from "react";
import ReactJsonView from "react-json-view";
import "typeface-roboto";
import {
  makeStyles,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Fab,
  TableHead,
  TableRow,
  Table,
  TableBody,
  TableCell,
  Tooltip,
  Typography
} from "@material-ui/core";
import { Send, Check, Email, Delete } from "@material-ui/icons";

const useStyles = makeStyles(theme => ({
  fab: {
    position: "fixed",
    bottom: theme.spacing(2),
    right: theme.spacing(2),
    margin: theme.spacing(1)
  }
}));

const socket = new WebSocket("ws://" + window.location.host + "/socket");

function ViewEvent({ event }) {
  const [open, setOpen] = React.useState(false);
  const handleClose = () => setOpen(false);

  const dialog = (
    <Dialog
      open={open}
      onClose={handleClose}
      scroll="paper"
      aria-labelledby="view-event-title"
      maxWidth="lg"
    >
      <DialogTitle id="view-event-title">Event</DialogTitle>
      <DialogContent dividers={true}>
        <ReactJsonView src={event} />
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} color="primary">
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );

  return (
    <div>
      <Button onClick={() => setOpen(true)}>
        <Email />
      </Button>
      {dialog}
    </div>
  );
}

function EventTypeIcon({ type }) {
  return (
    <Tooltip title={type}>{type === "SENT" ? <Send /> : <Check />}</Tooltip>
  );
}

function getMessages(page, size, callback) {
  fetch("/messages?page=" + page + "&size=" + size, {
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json"
    }
  })
    .then(response => {
      return response.json();
    })
    .then(messages => {
      callback(messages);
    });
}

function Activity() {
  const classes = useStyles();
  const [messages, setMessages] = React.useState([]);

  socket.onopen = () => {
    getMessages(0, 200, messages => {
      setMessages(messages);
    });
  };

  socket.onmessage = message => {
    getMessages(0, 200, messages => {
      setMessages(messages);
    });
  };

  const onClearEvents = () => {
    fetch("/messages", {
      method: "DELETE"
    }).then(() => {
      setMessages([]);
    });
  };

  return (
    <React.Fragment>
      <Fab
        variant="extended"
        color="secondary"
        aria-label="delete"
        className={classes.fab}
        onClick={onClearEvents}
      >
        <Delete className={classes.leftButton}>delete</Delete>
        Clear events
      </Fab>
      <Typography variant="h6">Activity</Typography>
      <Table className={classes.table} aria-label="activity table">
        <TableHead>
          <TableRow>
            <TableCell>ID</TableCell>
            <TableCell>Type</TableCell>
            <TableCell>Source</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Time</TableCell>
            <TableCell>Message</TableCell>
          </TableRow>
        </TableHead>
        {messages.length === 0 ? (
          <TableBody />
        ) : (
          <TableBody>
            {messages.map((message, rowId) => (
              <TableRow key={rowId}>
                <TableCell component="th" scope="row">
                  {message.id}
                </TableCell>
                <TableCell component="th" scope="row">
                  {message.event.attributes.type}
                </TableCell>
                <TableCell component="th" scope="row">
                  {message.event.attributes.source}
                </TableCell>
                <TableCell component="th" scope="row">
                  <EventTypeIcon type={message.type} />
                </TableCell>
                <TableCell component="th" scope="row">
                  {message.receivedAt}
                </TableCell>
                <TableCell component="th" scope="row">
                  <ViewEvent event={message.event} />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        )}
      </Table>
    </React.Fragment>
  );
}

export default Activity;