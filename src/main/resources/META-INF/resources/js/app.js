"use strict";

const {
  makeStyles,
  AppBar,
  Button,
  CssBaseline,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Divider,
  Drawer,
  Fab,
  Grid,
  Icon,
  IconButton,
  InputAdornment,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Toolbar,
  Tooltip,
  Typography
} = MaterialUI;

const ReactJsonView = reactJsonView.default;

const drawerWidth = 240;

const useStyles = makeStyles(theme => ({
  root: {
    display: "flex"
  },
  appBar: {
    zIndex: theme.zIndex.drawer + 1
  },
  drawer: {
    width: drawerWidth,
    flexShrink: 0
  },
  drawerPaper: {
    width: drawerWidth
  },
  content: {
    flexGrow: 1,
    padding: theme.spacing(3)
  },
  fab: {
    position: "fixed",
    bottom: theme.spacing(2),
    right: theme.spacing(2),
    margin: theme.spacing(1)
  },
  toolbar: theme.mixins.toolbar,
  container: {
    display: "flex",
    flexWrap: "wrap",
    width: 400
  },
  textField: {
    marginLeft: theme.spacing(1),
    marginRight: theme.spacing(1),
    width: 400
  }
}));

function SendEvent() {
  const classes = useStyles();
  const [values, setValues] = React.useState({
    message: JSON.stringify({ message: "Hello CloudEvents!" }, null, " ")
  });
  const [dirty, setDirty] = React.useState({});

  const generateId = () => onValueChanged(uuidv4(), "id");
  const onValueChanged = (value, field) => {
    setValues({ ...values, [field]: value });
    setDirty({ ...dirty, [field]: true });
  };

  const validate = (field, value) => {
    if (value === undefined || value === "") {
      return "This field is mandatory";
    }
    if (field === "message") {
      try {
        JSON.parse(value);
      } catch (e) {
        return "Doesn't look like a valid JSON";
      }
    }
    return null;
  };

  const showError = field =>
    dirty[field] && validate(field, values[field]) !== null;

  const sendEvent = () => {
    setDirty({
      id: true,
      type: true,
      source: true,
      message: true
    });
    let hasErrors = false;
    ["id", "type", "source", "message"].forEach(field => {
      if (validate(field, values[field]) !== null) {
        hasErrors = true;
      }
    });
    if (!hasErrors) {
      fetch("/messages", {
        method: "POST",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
          "ce-id": values.id,
          "ce-type": values.type,
          "ce-source": values.source,
          "ce-specversion": "0.3"
        },
        body: values.message
      });
    }
  };

  return (
    <React.Fragment>
      <Typography variant="h6">Create event</Typography>
      <form className={classes.container} noValidate autoComplete="off">
        <TextField
          required
          InputLabelProps={{
            shrink: values.id !== undefined && values.id !== ""
          }}
          error={showError("id")}
          helperText={validate("id", values.id)}
          label="Event ID"
          id="margin-normal"
          className={classes.textField}
          margin="normal"
          value={values.id || ""}
          onChange={event => onValueChanged(event.target.value, "id")}
          InputProps={{
            endAdornment: (
              <InputAdornment position="end">
                <IconButton
                  edge="end"
                  aria-label="Generate UUID"
                  onClick={generateId}
                >
                  <Icon>loop</Icon>
                </IconButton>
              </InputAdornment>
            )
          }}
        />
        <TextField
          required
          error={showError("type")}
          helperText={validate("type", values.type)}
          label="Event Type"
          id="margin-normal"
          className={classes.textField}
          margin="normal"
          value={values.type || ""}
          onChange={event => onValueChanged(event.target.value, "type")}
        />
        <TextField
          required
          error={showError("source")}
          helperText={validate("source", values.source)}
          label="Event Source"
          id="margin-normal"
          className={classes.textField}
          margin="normal"
          value={values.source || ""}
          onChange={event => onValueChanged(event.target.value, "source")}
        />
        <TextField
          required
          error={showError("message")}
          helperText={validate("message", values.message)}
          label="Message"
          id="margin-normal"
          className={classes.textField}
          margin="normal"
          multiline
          value={values.message}
          onChange={event => onValueChanged(event.target.value, "message")}
        />
        <Button
          variant="contained"
          className={classes.button}
          color="primary"
          onClick={sendEvent}
        >
          Send event
        </Button>
      </form>
    </React.Fragment>
  );
}

// Activity table
function EventTypeIcon({ type }) {
  return (
    <Tooltip title={type}>
      {type === "SENT" ? <Icon>send</Icon> : <Icon>check</Icon>}
    </Tooltip>
  );
}

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
        <Icon>email</Icon>
      </Button>
      {dialog}
    </div>
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

const socket = new WebSocket("ws://" + location.host + "/socket");

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
        <Icon className={classes.leftButton}>delete</Icon>
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
                  {message.event.type}
                </TableCell>
                <TableCell component="th" scope="row">
                  {message.event.source}
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

function Dashboard() {
  const classes = useStyles();

  return (
    <div className={classes.root}>
      <CssBaseline />
      <AppBar position="fixed" className={classes.appBar}>
        <Toolbar>
          <Typography variant="h6" noWrap>
            CloudEvents player
          </Typography>
        </Toolbar>
      </AppBar>
      <main className={classes.content}>
        <div className={classes.toolbar} />
        <Grid container className={classes.root} spacing={2}>
          <Grid item xs={4}>
            <SendEvent />
          </Grid>
          <Grid item xs={8}>
            <Activity />
          </Grid>
        </Grid>
      </main>
    </div>
  );
}

ReactDOM.render(
  React.createElement(Dashboard),
  document.getElementById("main")
);
