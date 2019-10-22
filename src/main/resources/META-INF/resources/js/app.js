"use strict";

const {
  CssBaseline,
  Typography,
  makeStyles,
  AppBar,
  Toolbar,
  Drawer,
  Divider,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Icon,
  TextField,
  InputAdornment,
  IconButton,
  Button
} = MaterialUI;

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
      fetch('/', {
        method: 'POST',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
          "ce-id": values.id,
          "ce-type": values.type,
          "ce-source": values.source,
          "ce-specversion": "0.3"
        },
        body: values.message
      })
    }
  };

  return (
    <React.Fragment>
      <form className={classes.container} noValidate autoComplete="off">
        <TextField
          required
          InputLabelProps={{ shrink: values.id  }}
          error={showError("id")}
          helperText={validate("id", values.id)}
          label="Event ID"
          id="margin-normal"
          className={classes.textField}
          margin="normal"
          value={values.id}
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
          value={values.type}
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
          value={values.source}
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

function Activity() {
  return <Typography paragraph>Activity</Typography>;
}

function Dashboard() {
  const classes = useStyles();
  const [selectedIndex, setSelectedIndex] = React.useState(0);

  const handleListItemClick = index => {
    setSelectedIndex(index);
  };

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
      <Drawer
        className={classes.drawer}
        variant="permanent"
        classes={{
          paper: classes.drawerPaper
        }}
      >
        <div className={classes.toolbar} />
        <List>
          <ListItem
            button
            selected={selectedIndex == 0}
            key="Send"
            onClick={() => handleListItemClick(0)}
          >
            <ListItemIcon>
              <Icon>send</Icon>
            </ListItemIcon>
            <ListItemText primary="Send" />
          </ListItem>
          <ListItem
            button
            selected={selectedIndex == 1}
            key="Activity"
            onClick={() => handleListItemClick(1)}
          >
            <ListItemIcon>
              <Icon>inbox</Icon>
            </ListItemIcon>
            <ListItemText primary="Activity" />
          </ListItem>
        </List>
      </Drawer>
      <main className={classes.content}>
        <div className={classes.toolbar} />
        {selectedIndex == 0 ? <SendEvent /> : <Activity />}
      </main>
    </div>
  );
}

ReactDOM.render(
  React.createElement(Dashboard),
  document.getElementById("main")
);
