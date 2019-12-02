import React from "react";
import { uuid } from "uuidv4";
import "typeface-roboto";
import {
  makeStyles,
  Button,
  FormControl,
  InputAdornment,
  IconButton,
  InputLabel,
  MenuItem,
  Select,
  TextField,
  Typography
} from "@material-ui/core";
import { Loop } from "@material-ui/icons";

const useStyles = makeStyles(theme => ({
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

const EventSender = () => {
  const classes = useStyles();
  const [values, setValues] = React.useState({
    specversion: "1.0",
    message: JSON.stringify({ message: "Hello CloudEvents!" }, null, " ")
  });
  const [dirty, setDirty] = React.useState({});

  const generateId = () => onValueChanged(uuid(), "id");
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
      specversion: true,
      message: true
    });
    let hasErrors = false;
    ["id", "type", "source", "specversion", "message"].forEach(field => {
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
          "ce-specversion": values.specversion
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
          id="id"
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
                  <Loop />
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
          id="type"
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
          id="source"
          className={classes.textField}
          margin="normal"
          value={values.source || ""}
          onChange={event => onValueChanged(event.target.value, "source")}
        />
        <FormControl className={classes.textField}>
          <InputLabel id="margin-normal">Specversion</InputLabel>
          <Select
            required
            error={showError("specversion")}
            label="Specversion"
            id="specversion"
            value={values.specversion || ""}
            onChange={event =>
              onValueChanged(event.target.value, "specversion")
            }
          >
            <MenuItem value="1.0">1.0</MenuItem>
            <MenuItem value="0.3">0.3</MenuItem>
            <MenuItem value="0.2">0.2</MenuItem>
          </Select>
        </FormControl>
        <TextField
          required
          error={showError("message")}
          helperText={validate("message", values.message)}
          label="Message"
          id="message"
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

export default EventSender;